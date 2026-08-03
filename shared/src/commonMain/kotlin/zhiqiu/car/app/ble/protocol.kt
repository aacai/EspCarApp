package zhiqiu.car.app.ble

import kotlin.math.pow
import kotlin.math.round
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 小车 BLE 协议常量与编解码（UUID / 指令 / 状态字段依据固件与 `docs/协议文档.md`）。 */

/** GATT UUID 常量（128-bit 形式，大小写不敏感）。 */
object CarUuids {
    const val SERVICE: String = "0000abcd-0000-1000-8000-00805f9b34fb"
    const val COMMAND: String = "00001234-0000-1000-8000-00805f9b34fb"
    const val STATUS: String = "00001235-0000-1000-8000-00805f9b34fb"
}

/** 五个基础运动方向。 */
enum class CarDirection {
    Forward,
    Backward,
    Left,
    Right,
    Stop,
}

/** 指令字符集（两种通道通用，固件大小写等价）。 */
object CarCommands {
    /** GAP 设备名前缀，扫描按此过滤。 */
    const val DEVICE_NAME_PREFIX: String = "EspCar_"

    /** 方向 → 指令字符。 */
    fun directionChar(direction: CarDirection): Char = when (direction) {
        CarDirection.Forward -> 'F'
        CarDirection.Backward -> 'B'
        CarDirection.Left -> 'L'
        CarDirection.Right -> 'R'
        CarDirection.Stop -> 'S'
    }

    /**
     * 速度百分比(0-100) → 速度档位字符('0'-'9')。
     * 固件仅接受单数字指令（数字×10%），故 100% 会被收敛到 '9'(90%)。
     */
    fun speedDigit(percent: Int): Char {
        val p = percent.coerceIn(0, 100)
        val digit = (p / 10).coerceIn(0, 9)
        return '0' + digit
    }

    /** 把一串指令字符编码为待写入 BLE 的字节数组（ASCII）。 */
    fun encode(vararg cmds: Char): ByteArray =
        cmds.concatToString().encodeToByteArray()

    /** 把指令字符串编码为字节数组。 */
    fun encode(text: String): ByteArray = text.encodeToByteArray()
}

/** 小车视角扫描到的 WiFi 接入点。 */
@Serializable
data class ScanAp(
    val ssid: String = "",
    val rssi: Int = 0,
    val chan: Int = 0,
    val auth: Int = 0,
    val target: Int = 0,
)

/** 小车实时状态，与 `docs/协议文档.md` §3 字段一一对应。 */
@Serializable
data class CarStatus(
    val ip: String = "0.0.0.0",
    val uptime: Long = 0,
    val free_heap: Long = 0,
    val last_cmd: String = "",
    val action: String = "停止",
    val speed: Int = 0,
    val cmd_count: Long = 0,
    val ble: Int = 0,
    @SerialName("ble_adv") val bleAdv: Int = -1,
    @SerialName("wifi_state") val wifiState: String = "off",
    val scan: List<ScanAp> = emptyList(),
    val wifi_log: List<String> = emptyList(),
)

/** 把状态 JSON 里的 `action` 中文映射回方向；无法识别返回 null。 */
fun CarStatus.toDirection(): CarDirection? = when (action) {
    "前进" -> CarDirection.Forward
    "后退" -> CarDirection.Backward
    "左转" -> CarDirection.Left
    "右转" -> CarDirection.Right
    "停止" -> CarDirection.Stop
    else -> null
}

/** 协议层专用的 Lenient JSON 解析器：容忍未知 key 与缺失字段。 */
val CarJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
}

/** 解析小车状态 JSON（来自 Read 全量或 Notify 完整分片）。 */
fun parseCarStatus(json: String): CarStatus = CarJson.decodeFromString(json)

/** [parseCarStatusTolerant] 的结果。[truncated] 为 true 表示原文被截断、只恢复了前半部分字段。 */
data class CarStatusParse(val status: CarStatus, val truncated: Boolean)

/**
 * 容错解析：状态 JSON 因 `scan` 数组常超 BLE ATT 上限而被截断；
 * 关键字段（ip/uptime/action/speed/...）都排在 `scan` 之前，
 * 故严格解析失败时扫描到顶层最后一个完整键值对边界补 `}` 再解析。
 * 完全无法恢复时返回 null。
 */
fun parseCarStatusTolerant(raw: String): CarStatusParse? {
    val start = raw.indexOf('{')
    if (start < 0) return null
    val body = raw.substring(start)
    runCatching { parseCarStatus(body) }.getOrNull()?.let { return CarStatusParse(it, false) }
    val cut = lastTopLevelPairEnd(body) ?: return null
    val repaired = body.substring(0, cut) + "}"
    return runCatching { parseCarStatus(repaired) }.getOrNull()
        ?.let { CarStatusParse(it, true) }
}

/** 找出顶层对象中「最后一个完整键值对之后的逗号」下标，用于截断修复；无完整键值对时返回 null。 */
private fun lastTopLevelPairEnd(body: String): Int? {
    var depth = 0
    var inString = false
    var escaped = false
    var last = -1
    for (i in body.indices) {
        val c = body[i]
        if (inString) {
            when {
                escaped -> escaped = false
                c == '\\' -> escaped = true
                c == '"' -> inString = false
            }
            continue
        }
        when (c) {
            '"' -> inString = true
            '{', '[' -> depth++
            '}', ']' -> depth--
            ',' -> if (depth == 1) last = i
        }
    }
    return if (last >= 0) last else null
}

/** 序列化状态为 JSON（主要用于测试与调试）。 */
fun CarStatus.toJson(): String = CarJson.encodeToString(this)

/** 把上电秒数格式化为 `1d 2h 3m 4s` 形式。 */
fun formatUptime(seconds: Long): String {
    if (seconds <= 0) return "0s"
    val d = seconds / 86400
    val h = (seconds % 86400) / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return buildString {
        if (d > 0) append("${d}d ")
        if (h > 0) append("${h}h ")
        if (m > 0) append("${m}m ")
        append("${s}s")
    }.trim()
}

/** 把字节数格式化为易读单位：B / KB / MB / GB（不足 1KB 时保留整数 B）。 */
fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "0B"
    val kb = bytes / 1024.0
    return when {
        bytes < 1024 -> "${bytes}B"
        kb < 1024 -> formatFixed(kb, 1) + "KB"
        kb < 1024 * 1024 -> formatFixed(kb / 1024.0, 2) + "MB"
        else -> formatFixed(kb / 1024.0 / 1024.0, 2) + "GB"
    }
}

/** 跨平台安全的定点数格式化（替代 JVM 专属的 String.format）。 */
private fun formatFixed(value: Double, decimals: Int): String {
    val factor = 10.0.pow(decimals.toDouble())
    val scaled = round(value * factor)
    val intPart = (scaled / factor).toLong()
    val frac = scaled.toLong().mod(factor.toLong()).let { if (it < 0) -it else it }
    return "$intPart." + frac.toString().padStart(decimals, '0')
}
