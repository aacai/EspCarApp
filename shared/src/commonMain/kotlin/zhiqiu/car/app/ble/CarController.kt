@file:OptIn(ExperimentalAtomicApi::class)

package zhiqiu.car.app.ble

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import zhiqiu.car.app.platformLog

/**
 * 业务逻辑控制器，桥接 [BleClient] 与 UI。
 *
 * 在架构上扮演 **ViewModel** 角色：持有 `CoroutineScope`、以 [StateFlow] 暴露全部界面状态、
 * 封装所有 BLE 业务与副作用，且为纯 Kotlin 类（不依赖 Compose）。因此各 Screen 无需再叠加
 * 一层 `lifecycle-viewmodel`，直接订阅此处的状态流即可。
 *
 * 安全兜底：松手即发 `S`、按住期间按 [KEEPALIVE_MS] 保活重发、看门狗超时补 `S`、断连复位。
 * 状态以 [STATUS_POLL_MS] 周期 Read 为主、Notify 为辅，均走容错解析 [parseCarStatusTolerant]
 * （状态 JSON 因 `scan` 数组超 BLE ATT 上限被截断，可恢复关键字段）。
 */

/** 进程启动基准时刻，用于看门狗计时（跨平台，不依赖 System.currentTimeMillis）。 */
private val START_MARK = TimeSource.Monotonic.markNow()

class CarController(
    private val client: BleClient,
     val settings: AppSettings = AppSettings(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: () -> Long = { START_MARK.elapsedNow().inWholeMilliseconds },
) {
    companion object {
        const val KEEPALIVE_MS: Long = 500
        const val WATCHDOG_TIMEOUT_MS: Long = 1500
        const val WATCHDOG_INTERVAL_MS: Long = 200
        const val STATUS_POLL_MS: Long = 2000
        private const val AUTO_RECONNECT_TIMEOUT_MS: Long = 8000
        /** 状态读取连续失败达到该次数即触发自动重连（应对 GATT 上下文被系统清掉）。 */
        private const val STATUS_FAIL_RECONNECT_THRESHOLD: Int = 3

        /** 分片重组缓冲上限，超过即判定为错位并丢弃重来。 */
        private const val MAX_STATUS_BUF: Int = 16 * 1024

        /** 界面调试日志保留条数。 */
        private const val MAX_LOG_LINES: Int = 80
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _scanDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val scanDevices: StateFlow<List<DiscoveredDevice>> = _scanDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _status = MutableStateFlow<CarStatus?>(null)
    val status: StateFlow<CarStatus?> = _status.asStateFlow()

    /** 状态读取/解析失败提示；为 null 表示正常。用于 UI 区分"正在获取"与"读取失败"。 */
    private val _statusError = MutableStateFlow<String?>(null)
    val statusError: StateFlow<String?> = _statusError.asStateFlow()

    /** 分片状态缓冲：小车按 MTU 分片推送状态，逐片累积成完整 JSON 再解析。 */
    private val statusBuf = StringBuilder()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** 调试日志（最近 N 条），既打到 logcat（[EspCar] 标签），也供 UI 界面显示。 */
    private val _logLines = MutableStateFlow<List<String>>(emptyList())
    val logLines: StateFlow<List<String>> = _logLines.asStateFlow()
    private fun log(msg: String) {
        val line = "[${clock()}] $msg"
        platformLog("EspCar", line)
        // 多个协程（observe / poll / keepAlive）会并发写日志，必须原子更新避免丢条目。
        _logLines.update { (it + line).takeLast(MAX_LOG_LINES) }
    }

    private val _currentDirection = MutableStateFlow<CarDirection?>(null)
    val currentDirection: StateFlow<CarDirection?> = _currentDirection.asStateFlow()

    val isSupported: Boolean get() = client.isSupported
    val discoveryMode: DiscoveryMode get() = client.discoveryMode

    private var connection: BleConnection? = null
    private var scanJob: Job? = null
    private var stateJob: Job? = null
    private var observeJob: Job? = null
    private var pollJob: Job? = null
    private var keepAliveJob: Job? = null
    private var watchdogJob: Job? = null

    private val lastCommandChar = AtomicReference('S')
    private val lastCommandAt = AtomicLong(0)

    // region 扫描
    /**
     * 开始扫描。`timeoutMs` 非空时，扫描到时自动停止以节省电量；
     * 传 null 则不自动停（用于内部自动重连场景，由调用方自行控制停止时机）。
     */
    fun startScan(timeoutMs: Long? = null) {
        if (_isScanning.value) return
        scanJob?.cancel()
        scanJob = scope.launch {
            _error.value = null
            _isScanning.value = true
            _scanDevices.value = emptyList()
            val timeoutJob = if (timeoutMs != null && timeoutMs > 0) {
                launch {
                    delay(timeoutMs)
                    log("startScan: 扫描超时，自动停止以省电")
                    stopScan()
                }
            } else null
            runCatching {
                client.startScan(ScanFilter.NamePrefix(settings.scanNamePrefix)).collect { device ->
                    // 过滤无名字设备（开启时，设备名为 null 直接跳过）
                    if (settings.filterNamelessEnabled && device.name == null) return@collect
                    val list = _scanDevices.value.toMutableList()
                    val idx = list.indexOfFirst { it.id == device.id }
                    if (idx >= 0) list[idx] = device else list.add(device)
                    _scanDevices.value = list
                }
            }.onFailure { e ->
                if (e !is CancellationException) {
                    // 正常的停止扫描（手动 stopScan / 离开页面 / 自动超时）会取消协程，
                    // 这属于预期行为，不要当成“扫描失败”上报。
                    _error.value = "扫描失败：${e.message}"
                }
            }
            timeoutJob?.cancel()
            _isScanning.value = false
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }
    // endregion

    // region 连接
    /** 非挂起入口：供 UI 点击事件调用，协程运行在 controller 自身 scope，不随组合销毁被取消。 */
    fun connectDevice(device: DiscoveredDevice) {
        scope.launch { connect(device) }
    }

    suspend fun connect(device: DiscoveredDevice) {
        stopScan()
        _error.value = null
        _connectionState.value = ConnectionState.Connecting
        runCatching {
            val conn = client.connect(device)
            connection = conn
            log("connect OK name=${device.name} id=${device.id}")
            stateJob = scope.launch {
                conn.state.collect { st ->
                    _connectionState.value = st
                    if (st == ConnectionState.Disconnected) onDisconnected()
                }
            }
            observeJob = scope.launch {
                runCatching {
                    log("observe: start collecting status notifications")
                    conn.observeStatus().collect { onNotify(it) }
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    // 状态通知流异常（如 GATT 忙）不应崩溃应用；
                    // 状态仍由 statusPollLoop 周期性 read 兜底刷新。
                    log("observe FAIL (notifications not delivered): ${e.message}")
                }
            }
            // 先让通知订阅（CCCD 描述符写入）完成，再发起读，错开 GATT 时序，
            // 避免 Android BluetoothGatt 并发操作抛 WriteRequestBusy。
            delay(600)
            pollJob = scope.launch { statusPollLoop(conn) }
            if (settings.watchdogEnabled) startWatchdog()
            refreshStatus(conn)
            settings.rememberDevice(device.id, device.name)
        }.onFailure { e ->
            if (e !is Exception) throw e
            _error.value = "连接失败：${e.message}"
            log("connect FAIL: ${e.message}")
            _connectionState.value = ConnectionState.Disconnected
            cleanupConnection()
        }
    }

    suspend fun disconnect() {
        stopScan()
        releaseDirectionInternal()
        val conn = connection
        if (conn != null) {
            // 忽略异常，本地状态随后复位
            runCatching { conn.disconnect() }
        }
        cleanupConnection()
        _connectionState.value = ConnectionState.Disconnected
        _status.value = null
        _statusError.value = null
    }

    /** 进程/页面退出前尽力让车停下。 */
    suspend fun emergencyStop() {
        releaseDirectionInternal()
        val conn = connection
        if (conn != null) {
            // 已断连则忽略
            runCatching { conn.writeCommand(CarCommands.encode('S')) }
        }
    }

    fun clearError() {
        _error.value = null
    }
    // endregion

    // region 控制
    /** 按下某方向：立即发送一次，并启动保活重发。 */
    fun pressDirection(direction: CarDirection) {
        _currentDirection.value = direction
        markCommand(CarCommands.directionChar(direction))
        scope.launch { sendCommand(lastCommandChar.load()) }
        startKeepAlive(direction)
    }

    /** 松开：停止并取消保活。 */
    fun releaseDirection() {
        releaseDirectionInternal()
    }

    /** 设置速度（0-100 百分比）。 */
    fun setSpeed(percent: Int) {
        scope.launch { sendCommand(CarCommands.speedDigit(percent)) }
    }

    private fun releaseDirectionInternal() {
        keepAliveJob?.cancel()
        keepAliveJob = null
        _currentDirection.value = null
        markCommand('S')
        scope.launch { sendCommand('S') }
    }

    private fun startKeepAlive(direction: CarDirection) {
        keepAliveJob?.cancel()
        keepAliveJob = scope.launch {
        while (isActive) {
            delay(KEEPALIVE_MS)
            val ch = CarCommands.directionChar(direction)
            markCommand(ch)
            sendCommand(ch)
        }
        }
    }
    // endregion

    // region 状态刷新与看门狗
    /**
     * 主动 Read 一次状态（主路径）。状态 JSON 因 `scan` 数组超 BLE ATT 上限被截断，
     * 须走容错解析——截断只丢 `scan`/`wifi_log`，关键字段都在其之前。
     */
    private suspend fun refreshStatus(conn: BleConnection): Boolean {
        val json = conn.readStatus()
        if (json == null) {
            log("refreshStatus: read=null, characteristic may not support Read or GATT context lost")
            _statusError.value = "状态读取失败：小车未返回数据（可能蓝牙连接已失效）"
            return false
        }
        val parsed = parseCarStatusTolerant(json)
        if (parsed == null) {
            log("refreshStatus parse FAIL len=${json.length} peek=${json.take(80)}")
            _statusError.value = "状态解析失败（小车返回数据异常）"
            return false
        }
        _status.value = parsed.status
        _statusError.value = null
        log(
            "refreshStatus OK len=${json.length} truncated=${parsed.truncated} " +
                "action=${parsed.status.action} speed=${parsed.status.speed} cmd=${parsed.status.last_cmd}",
        )
        return true
    }

    /**
     * 处理一条状态通知（仅 observe 协程内调用，[statusBuf] 无并发访问）。
     * 兼容单包完整 JSON 与多包分片：累积重组，遇新包（以 `{` 开头）重置缓冲避免错位；
     * 容错解析出关键字段即刷新 UI，严格解析成功才清空缓冲。
     */
    private fun onNotify(raw: String) {
        if (raw.startsWith("{")) statusBuf.clear()
        if (statusBuf.length > MAX_STATUS_BUF) {
            log("onNotify: buffer overflow(${statusBuf.length})，丢弃重来")
            statusBuf.clear()
        }
        statusBuf.append(raw)
        val buf = statusBuf.toString()
        val parsed = parseCarStatusTolerant(buf) ?: parseCarStatusTolerant(raw)
        if (parsed == null) {
            log("onNotify partial raw.len=${raw.length} buf.len=${buf.length} peek=${buf.take(80)}")
            return
        }
        _status.value = parsed.status
        _statusError.value = null
        if (!parsed.truncated) statusBuf.clear()
        log(
            "onNotify OK raw.len=${raw.length} buf.len=${buf.length} truncated=${parsed.truncated} " +
                "action=${parsed.status.action} speed=${parsed.status.speed} cmd=${parsed.status.last_cmd}",
        )
    }

    private suspend fun statusPollLoop(conn: BleConnection) {
        var consecutiveFails = 0
        var ticks = 0
        while (scope.isActive) {
            delay(STATUS_POLL_MS)
            ticks++
            // 运动中保活指令每 KEEPALIVE_MS 一发，会争抢 GATT 锁；但用户仍需看到实时状态，
            // 故降频读取（每 5 轮一次）而非完全跳过，松手后因 ticks%5 自然恢复全频。
            val moving = _currentDirection.value != null
            if (moving && ticks % 5 != 0) continue
            val ok = refreshStatus(conn)
            if (ok) {
                consecutiveFails = 0
            } else {
                consecutiveFails++
                // GATT 上下文可能被系统清掉（小米抢连 op_code=13），连续失败需自动重连恢复。
                if (consecutiveFails >= STATUS_FAIL_RECONNECT_THRESHOLD) {
                    log("statusPollLoop: 连续 ${consecutiveFails} 次读取失败，触发自动重连")
                    consecutiveFails = 0
                    reconnectViaScan()
                    return // 重连流程会重建连接并重启轮询
                }
            }
        }
    }

    private fun startWatchdog() {
        if (watchdogJob?.isActive == true) return
        watchdogJob = scope.launch {
            while (isActive) {
                delay(WATCHDOG_INTERVAL_MS)
                if (!settings.watchdogEnabled) continue
                val conn = connection ?: continue
                val moving = _status.value?.action != "停止"
                val stale = clock() - lastCommandAt.load() > WATCHDOG_TIMEOUT_MS
                if (moving && stale && lastCommandChar.load() != 'S') {
                    runCatching { conn.writeCommand(CarCommands.encode('S')) }
                    markCommand('S')
                    _currentDirection.value = null
                }
            }
        }
    }

    private fun stopWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
    }
    // endregion

    private fun onDisconnected() {
        keepAliveJob?.cancel()
        keepAliveJob = null
        stopWatchdog()
        _currentDirection.value = null
        lastCommandChar.store('S')
        lastCommandAt.store(0)
        _status.value = null
        _statusError.value = null
        statusBuf.clear()
        cleanupConnection()
    }

    private fun cleanupConnection() {
        stateJob?.cancel(); stateJob = null
        observeJob?.cancel(); observeJob = null
        pollJob?.cancel(); pollJob = null
        stopWatchdog()
        connection = null
    }

    /** 记录最近一次发出的指令字符与时间戳，看门狗与保活据此判断指令是否「陈旧」。 */
    private fun markCommand(char: Char) {
        lastCommandChar.store(char)
        lastCommandAt.store(clock())
    }

    private suspend fun sendCommand(char: Char) {
        val conn = connection ?: return
        runCatching {
            conn.writeCommand(CarCommands.encode(char))
            log("sendCommand '$char' OK")
        }.onFailure { e ->
            log("sendCommand '$char' FAIL: ${e.message}")
            // 写入失败（如断连）忽略，状态流会反映断连
        }
    }

    /**
     * 开始扫描并在 [timeoutMs] 内等待匹配 [deviceId] 的设备出现，找到返回设备、超时返回 null。
     * 用于「记住设备」的自动重连，以及状态读取失败后的连接重建。
     */
    private suspend fun scanUntilFound(deviceId: String, timeoutMs: Long): DiscoveredDevice? {
        startScan()
        val deadline = clock() + timeoutMs
        return try {
            var found: DiscoveredDevice? = null
            while (scope.isActive && clock() < deadline && found == null) {
                found = _scanDevices.value.firstOrNull { it.id == deviceId }
                if (found == null) delay(200)
            }
            found
        } finally {
            if (_connectionState.value != ConnectionState.Connected) stopScan()
        }
    }

    /** 自动重连上次设备：扫描并在超时内匹配到 remembered id 即连接。 */
    suspend fun autoReconnectIfNeeded() {
        if (!settings.autoReconnect) return
        val id = settings.lastDeviceId ?: return
        scanUntilFound(id, AUTO_RECONNECT_TIMEOUT_MS)?.let { connect(it) }
    }

    /**
     * 状态读取连续失败后重建连接：系统蓝牙栈（小米抢连 op_code=13）会清掉 App 的 GATT
     * 上下文，导致 Read/Notify 全部静默失败。此时扫描重连是恢复状态的唯一手段。
     * 不依赖设置开关，因为这是为了恢复已失效的连接。
     */
    private suspend fun reconnectViaScan() {
        cleanupConnection()
        val id = settings.lastDeviceId ?: run {
            _statusError.value = "状态读取失败且无连接记录，请返回扫描页手动连接"
            return
        }
        val found = scanUntilFound(id, AUTO_RECONNECT_TIMEOUT_MS)
        if (found != null) {
            connect(found)
        } else {
            _statusError.value = "自动重连超时：未找到小车，请手动连接"
        }
    }

    /**
     * 释放所有资源：取消内部 [CoroutineScope]，进而停止看门狗、保活、状态轮询等全部协程。
     *
     * 本类为 App 级单例（由 [App] 以 `remember` 持有），生命周期等同进程，通常无需手动调用；
     * 但若在非 App 级场景复用，或在平台入口（如 Android `Activity.onDestroy`）需要确定性释放时，
     * 可显式调用本方法。
     */
    fun close() {
        scope.cancel()
    }

    /**
     * 仅供单元测试：模拟一次"未被保活刷新、但本地仍认为在运动"的指令，
     * 使看门狗判定为陈旧并发补 `S`。生产代码不会调用。
     */
    @Suppress("unused")
    internal fun testSimulateStaleMovement(direction: CarDirection) {
        lastCommandChar.store(CarCommands.directionChar(direction))
        lastCommandAt.store(clock() - WATCHDOG_TIMEOUT_MS - 100)
    }
}