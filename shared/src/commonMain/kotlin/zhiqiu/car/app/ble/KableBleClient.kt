package zhiqiu.car.app.ble

import com.juul.kable.Advertisement
import com.juul.kable.Characteristic
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.WriteType
import kotlinx.coroutines.flow.Flow
import zhiqiu.car.app.platformLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.uuid.Uuid

/**
 * 基于 Kable 的真实 [BleClient] 实现，全平台共用。
 * 扫描按名称前缀 `EspCar_` 过滤；状态读取采用"通知触发 + 主动 Read 全量"以规避 MTU 分片。
 */
class KableBleClient : BleClient {

    private companion object {
        const val SERVICE_DISCOVERY_TIMEOUT_MS = 5_000L
    }

    private val advertisementsById = mutableMapOf<String, Advertisement>()

    override val isSupported: Boolean get() = platformBleSupported
    override val discoveryMode: DiscoveryMode get() = platformDiscoveryMode

    override fun startScan(filter: ScanFilter): Flow<DiscoveredDevice> =
        Scanner {
            filters {
                // 空前缀 = 不过滤（扫描所有设备）
                filter.namePrefix?.takeIf { it.isNotBlank() }?.let { prefix ->
                    match { name = Filter.Name.Prefix(prefix) }
                }
                filter.serviceUuid?.let { uuid ->
                    match { services = listOf(Uuid.parse(uuid)) }
                }
            }
        }.advertisements.map { advertisement ->
            @Suppress("RedundantConversion")
            val id = advertisement.identifier.toString()
            advertisementsById[id] = advertisement
            DiscoveredDevice(id, advertisement.name, advertisement.rssi)
        }

    override suspend fun pickDevice(): DiscoveredDevice? =
        throw UnsupportedOperationException("当前平台使用扫描模式（DiscoveryMode.Scan），请调用 startScan")

    override suspend fun connect(device: DiscoveredDevice): BleConnection {
        val advertisement = advertisementsById[device.id]
            ?: throw BleException("未找到设备 ${device.name ?: device.id} 的广播记录，请重新扫描后再连接")
        val peripheral = Peripheral(advertisement)
        try {
            peripheral.connect()
        } catch (e: Exception) {
            throw BleException("连接失败：${e.message}", e)
        }
        val services = withTimeoutOrNull(SERVICE_DISCOVERY_TIMEOUT_MS) {
            peripheral.services.first { !it.isNullOrEmpty() }
        } ?: throw BleException("GATT 服务发现超时（${SERVICE_DISCOVERY_TIMEOUT_MS}ms），请重试连接")
        val discovered = services.flatMap { it.characteristics }
        platformLog("EspCar", "services discovered=${services.size} characteristics=${discovered.size}")
        val statusChar = discovered
            .firstOrNull { it.characteristicUuid == Uuid.parse(CarUuids.STATUS) }
            ?: throw BleException("STATUS characteristic (0x1235) not found in discovered services")
        val commandChar = discovered
            .firstOrNull { it.characteristicUuid == Uuid.parse(CarUuids.COMMAND) }
            ?: throw BleException("COMMAND characteristic (0x1234) not found in discovered services")
        return KableBleConnection(peripheral, commandChar, statusChar)
    }

    /** 清除已缓存的广播记录（例如断连后下次连接前刷新）。 */
    fun forgetAdvertisement(id: String) {
        advertisementsById.remove(id)
    }
}

/**
 * 基于 Kable [Peripheral] 的连接句柄。
 * [requestMtu] 为空实现（Kable 公共接口未暴露）；分片问题靠"通知触发 + 主动 Read 全量"解决。
 */
class KableBleConnection(
    private val peripheral: Peripheral,
    private val commandCharacteristic: Characteristic,
    private val statusCharacteristic: Characteristic,
) : BleConnection {

    // Android BluetoothGatt 同一时刻只允许一个操作在飞，用 Mutex 把所有读写串行化。
    private val gattMutex = Mutex()

    override val state: Flow<ConnectionState> = peripheral.state.map { kableState ->
        when (kableState) {
            is State.Connected -> ConnectionState.Connected
            is State.Disconnected -> ConnectionState.Disconnected
            else -> ConnectionState.Connecting
        }
    }

    override suspend fun requestMtu(mtu: Int) {
        // Kable 公共接口未提供 requestMtu；依赖 Read 全量规避 MTU 分片。
    }

    override suspend fun writeCommand(bytes: ByteArray) {
        try {
            gattMutex.withLock {
                peripheral.write(commandCharacteristic, bytes, WriteType.WithoutResponse)
            }
        } catch (e: Exception) {
            throw BleException("指令写入失败：${e.message}", e)
        }
    }

    override suspend fun readStatus(): String? = try {
        gattMutex.withLock { peripheral.read(statusCharacteristic).decodeToString() }
    } catch (e: Exception) {
        platformLog("EspCar", "readStatus FAIL: ${e.message}")
        null
    }

    override fun observeStatus(): Flow<String> =
        peripheral.observe(statusCharacteristic, onSubscription = {
            platformLog("EspCar", "OBSERVE subscription requested (enabling notifications)")
        }).map { bytes ->
            val hex = bytes.take(64).joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
            platformLog("EspCar", "RAW len=${bytes.size} hex=$hex str=${bytes.decodeToString().take(120)}")
            bytes.decodeToString()
        }

    override suspend fun disconnect() {
        try {
            peripheral.disconnect()
        } catch (_: Exception) {
            // 断连失败也忽略，本地状态由控制器复位
        }
    }
}