package zhiqiu.car.app.ble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 测试用示例状态 JSON（与固件字段一致）。 */
val SAMPLE_STATUS_JSON: String = """
{
  "ip":"192.168.50.100",
  "uptime":373,
  "free_heap":99520,
  "last_cmd":"F",
  "action":"前进",
  "speed":60,
  "cmd_count":5,
  "ble":1,
  "ble_adv":0,
  "wifi_state":"connected",
  "scan":[{"ssid":"MyAP","rssi":-44,"chan":5,"auth":4,"target":1}],
  "wifi_log":["scan start","connected"]
}
""".trimIndent()

/**
 * 不触达真实蓝牙的内存版 [BleClient]，供单元测试与 Compose UI 测试使用。
 * 行为可由测试脚本驱动：注入状态、模拟断连、设置连接失败。
 */
class FakeBleClient(
    val supported: Boolean = true,
    val mode: DiscoveryMode = DiscoveryMode.Scan,
    val devices: List<DiscoveredDevice> = listOf(
        DiscoveredDevice("AA:BB:CC:DD:EE:01", "EspCar_ABCDEF", -50),
        DiscoveredDevice("AA:BB:CC:DD:EE:02", "EspCar_123456", -60),
    ),
) : BleClient {

    var lastConnectedId: String? = null
    var lastConnectedName: String? = null
    val writtenCommands: MutableList<ByteArray> = mutableListOf()
    var shouldFailConnect: Boolean = false
    var lastFilter: ScanFilter? = null

    override val isSupported: Boolean get() = supported
    override val discoveryMode: DiscoveryMode get() = mode

    override fun startScan(filter: ScanFilter): Flow<DiscoveredDevice> = kotlinx.coroutines.flow.flow {
        lastFilter = filter
        val prefix = filter.namePrefix
        devices
            .filter { d -> prefix?.let { d.name?.startsWith(it) } ?: true }
            .forEach { emit(it) }
    }

    override suspend fun pickDevice(): DiscoveredDevice? = devices.firstOrNull()

    override suspend fun connect(device: DiscoveredDevice): BleConnection {
        if (shouldFailConnect) throw BleException("simulated connect failure")
        lastConnectedId = device.id
        lastConnectedName = device.name
        return FakeBleConnection(device, this)
    }
}

/**
 * 测试用连接句柄：状态与通知均可由测试脚本驱动。
 * 不自动推送状态，控制器连接后应主动 readStatus 拉取全量。
 */
class FakeBleConnection(
    val device: DiscoveredDevice,
    private val client: FakeBleClient,
) : BleConnection {

    private val stateFlow = MutableStateFlow(ConnectionState.Connecting)
    private val statusFlow = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val scope = CoroutineScope(Dispatchers.Default)

    var disconnected: Boolean = false
    var requestedMtu: Int? = null

    init {
        scope.launch {
            stateFlow.value = ConnectionState.Connected
        }
    }

    override val state: Flow<ConnectionState> get() = stateFlow.asStateFlow()
    override suspend fun requestMtu(mtu: Int) { requestedMtu = mtu }
    override suspend fun writeCommand(bytes: ByteArray) { client.writtenCommands.add(bytes) }
    override suspend fun readStatus(): String? = SAMPLE_STATUS_JSON
    override fun observeStatus(): Flow<String> = statusFlow
    override suspend fun disconnect() {
        disconnected = true
        stateFlow.value = ConnectionState.Disconnected
    }

    /** 测试用：模拟小车上报一次状态更新。 */
    fun simulateStatus(json: String) {
        statusFlow.tryEmit(json)
    }

    /** 测试用：模拟异常断连。 */
    fun simulateDrop() {
        stateFlow.value = ConnectionState.Disconnected
    }
}
