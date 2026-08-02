package zhiqiu.car.app.ble

import kotlinx.coroutines.flow.Flow

/** 设备发现方式：S扫（系统扫描）或 P选（系统选择器，Web 专用）。 */
public enum class DiscoveryMode {
    Scan,
    Pick,
}

/** 扫描过滤条件。固件广播不含 Service UUID，故实际只用 NamePrefix。 */
public sealed interface ScanFilter {
    public val namePrefix: String? get() = null
    public val serviceUuid: String? get() = null

    public data class NamePrefix(val prefix: String) : ScanFilter {
        override val namePrefix: String get() = prefix
    }

    public data class ServiceUuid(val uuid: String) : ScanFilter {
        override val serviceUuid: String get() = uuid
    }
}

/** 扫描到的设备。 */
public data class DiscoveredDevice(
    val id: String,
    val name: String?,
    val rssi: Int? = null,
)

/** 连接状态机。 */
public enum class ConnectionState {
    Connecting,
    Connected,
    Disconnected,
}

/** BLE 操作失败。 */
public class BleException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** 已建立的连接句柄。 */
public interface BleConnection {
    /** 连接状态变化流。 */
    public val state: Flow<ConnectionState>

    /** 请求更大 MTU（可选，失败不影响后续读写）。 */
    public suspend fun requestMtu(mtu: Int)

    /** 写入控制指令（WriteWithoutResponse）。 */
    public suspend fun writeCommand(bytes: ByteArray)

    /** 主动读取完整状态 JSON（Read 全量，规避 MTU 分片）。 */
    public suspend fun readStatus(): String?

    /** 订阅状态通知。每次 Notify 触发发射一条（内容可能为分片，控制器应随后 readStatus）。 */
    public fun observeStatus(): Flow<String>

    /** 主动断开。 */
    public suspend fun disconnect()
}

/** 平台无关的 BLE 客户端抽象，真实实现（Kable）与测试替身（Fake）共用此接口。 */
public interface BleClient {
    /** 当前平台是否支持 BLE。 */
    public val isSupported: Boolean

    /** 当前平台的设备发现方式。 */
    public val discoveryMode: DiscoveryMode

    /**
     * 扫描模式：开始扫描并返回设备流；取消 Flow 收集即停止扫描。
     * Pick 模式下不应调用。
     */
    public fun startScan(
        filter: ScanFilter = ScanFilter.NamePrefix(CarCommands.DEVICE_NAME_PREFIX),
    ): Flow<DiscoveredDevice>

    /**
     * 选取模式：弹出系统选择器让用户选取设备；取消选取返回 null。
     * Scan 模式下不应调用。
     */
    public suspend fun pickDevice(): DiscoveredDevice?

    /** 连接设备，返回连接句柄。 */
    public suspend fun connect(device: DiscoveredDevice): BleConnection
}
