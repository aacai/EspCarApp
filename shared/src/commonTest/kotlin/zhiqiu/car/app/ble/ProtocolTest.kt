package zhiqiu.car.app.ble

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProtocolTest {

    @Test
    fun uuidsUseBluetoothBaseUuid() {
        assertEquals("0000abcd-0000-1000-8000-00805f9b34fb", CarUuids.SERVICE)
        assertEquals("00001234-0000-1000-8000-00805f9b34fb", CarUuids.COMMAND)
        assertEquals("00001235-0000-1000-8000-00805f9b34fb", CarUuids.STATUS)
    }

    @Test
    fun deviceNamePrefix() {
        assertEquals("EspCar_", CarCommands.DEVICE_NAME_PREFIX)
    }

    @Test
    fun directionChars() {
        assertEquals('F', CarCommands.directionChar(CarDirection.Forward))
        assertEquals('B', CarCommands.directionChar(CarDirection.Backward))
        assertEquals('L', CarCommands.directionChar(CarDirection.Left))
        assertEquals('R', CarCommands.directionChar(CarDirection.Right))
        assertEquals('S', CarCommands.directionChar(CarDirection.Stop))
    }

    @Test
    fun speedDigitClampsToSingleDigit() {
        assertEquals('0', CarCommands.speedDigit(0))
        assertEquals('5', CarCommands.speedDigit(50))
        assertEquals('9', CarCommands.speedDigit(90))
        assertEquals('9', CarCommands.speedDigit(100)) // 固件最高 90%
        assertEquals('0', CarCommands.speedDigit(-5))
        assertEquals('9', CarCommands.speedDigit(95))
    }

    @Test
    fun encodeCommands() {
        assertEquals("FL", CarCommands.encode('F', 'L').decodeToString())
        assertEquals(
            "S",
            CarCommands.encode(CarCommands.directionChar(CarDirection.Stop)).decodeToString(),
        )
        assertEquals("8", CarCommands.encode(CarCommands.speedDigit(80)).decodeToString())
    }

    @Test
    fun parseFullStatus() {
        val json = """
        {"ip":"192.168.50.100","uptime":373,"free_heap":99520,
         "last_cmd":"F","action":"前进","speed":60,"cmd_count":1,
         "ble":1,"ble_adv":0,"wifi_state":"connected",
         "scan":[{"ssid":"AP","rssi":-44,"chan":5,"auth":4,"target":1}],
         "wifi_log":["a","b"]}
        """.trimIndent()

        val s = parseCarStatus(json)
        assertEquals("192.168.50.100", s.ip)
        assertEquals(373, s.uptime)
        assertEquals(99520, s.free_heap)
        assertEquals("F", s.last_cmd)
        assertEquals("前进", s.action)
        assertEquals(60, s.speed)
        assertEquals(1, s.cmd_count)
        assertEquals(1, s.ble)
        assertEquals(0, s.bleAdv)
        assertEquals("connected", s.wifiState)
        assertEquals(1, s.scan.size)
        assertEquals("AP", s.scan[0].ssid)
        assertEquals(-44, s.scan[0].rssi)
        assertEquals(2, s.wifi_log.size)
        assertEquals(CarDirection.Forward, s.toDirection())
    }

    @Test
    fun parseMissingFieldsUsesDefaults() {
        val s = parseCarStatus("{}")
        assertEquals("0.0.0.0", s.ip)
        assertEquals("停止", s.action)
        assertEquals(-1, s.bleAdv)
        assertEquals("off", s.wifiState)
        assertTrue(s.scan.isEmpty())
        assertTrue(s.wifi_log.isEmpty())
        assertEquals(CarDirection.Stop, s.toDirection())
    }

    @Test
    fun parseIgnoresUnknownKeys() {
        val s = parseCarStatus("""{"foo":1,"speed":80,"action":"左转"}""")
        assertEquals(80, s.speed)
        assertEquals(CarDirection.Left, s.toDirection())
    }

    @Test
    fun roundTripStatus() {
        val original = CarStatus(
            ip = "1.2.3.4",
            uptime = 12,
            speed = 70,
            action = "右转",
            bleAdv = 0,
            wifiState = "scanning",
            scan = listOf(ScanAp(ssid = "x", rssi = -50, chan = 6, auth = 3, target = 0)),
        )
        val restored = parseCarStatus(original.toJson())
        assertEquals(original, restored)
    }

    /**
     * 复刻线上实测样本：BLE Read 只回 596 字节，JSON 断在 `scan[7].ssid`，
     * 严格解析必然抛 `Expected quotation mark '"', but had 'EOF'`。
     * 容错解析必须能恢复 scan 之前的全部关键字段。
     */
    @Test
    fun tolerantParseRecoversTruncatedStatus() {
        val head = """{"ip":"192.168.50.100","uptime":373,"free_heap":99520,""" +
            """"last_cmd":"F","action":"前进","speed":60,"cmd_count":12,""" +
            """"ble":1,"ble_adv":0,"wifi_state":"connected","scan":["""
        val aps = (0 until 7).joinToString(",") {
            """{"ssid":"AP-$it","rssi":${-40 - it},"chan":${it + 1},"auth":4,"target":0}"""
        }
        // 第 8 项的 ssid 字符串被 MTU/ATT 截断（引号未闭合）
        val truncated = head + aps + """,{"ssid":"ImmortalWrt-2."""

        assertTrue(runCatching { parseCarStatus(truncated) }.isFailure, "严格解析应当失败")

        val parsed = assertNotNull(parseCarStatusTolerant(truncated), "容错解析不应返回 null")
        assertTrue(parsed.truncated, "应标记为截断恢复")
        val s = parsed.status
        assertEquals("192.168.50.100", s.ip)
        assertEquals(373, s.uptime)
        assertEquals("F", s.last_cmd)
        assertEquals("前进", s.action)
        assertEquals(60, s.speed)
        assertEquals(12, s.cmd_count)
        assertEquals(1, s.ble)
        assertEquals("connected", s.wifiState)
        assertEquals(CarDirection.Forward, s.toDirection())
        // 被截断的 scan 退化为空列表，不影响控制关键字段
        assertTrue(s.scan.isEmpty())
    }

    @Test
    fun tolerantParseKeepsCompleteJsonIntact() {
        val json = """{"ip":"1.2.3.4","action":"左转","speed":30,""" +
            """"scan":[{"ssid":"A","rssi":-50,"chan":1,"auth":0,"target":1}],"wifi_log":["x"]}"""
        val parsed = assertNotNull(parseCarStatusTolerant(json))
        assertFalse(parsed.truncated, "完整 JSON 不应标记为截断")
        assertEquals("左转", parsed.status.action)
        assertEquals(1, parsed.status.scan.size)
        assertEquals(1, parsed.status.wifi_log.size)
    }

    /** 截断发生在 wifi_log 时，scan 应完整保留。 */
    @Test
    fun tolerantParseRecoversWhenTruncatedInWifiLog() {
        val json = """{"action":"后退","speed":40,""" +
            """"scan":[{"ssid":"A","rssi":-50,"chan":1,"auth":0,"target":1}],"wifi_log":["已启动","连接"""
        val parsed = assertNotNull(parseCarStatusTolerant(json))
        assertTrue(parsed.truncated)
        assertEquals("后退", parsed.status.action)
        assertEquals(1, parsed.status.scan.size, "scan 已完整到达，应当保留")
    }

    /** 含转义引号的 ssid 不能把括号扫描带偏。 */
    @Test
    fun tolerantParseHandlesEscapedQuotes() {
        val json = """{"action":"停止","last_cmd":"S","wifi_state":"off",""" +
            """"scan":[{"ssid":"my\"ap{,","rssi":-3"""
        val parsed = assertNotNull(parseCarStatusTolerant(json))
        assertEquals("停止", parsed.status.action)
        assertEquals("off", parsed.status.wifiState)
    }

    @Test
    fun tolerantParseReturnsNullOnUnrecoverableInput() {
        assertNull(parseCarStatusTolerant(""))
        assertNull(parseCarStatusTolerant("not json at all"))
        // 第一个键值对都没读完，无法恢复
        assertNull(parseCarStatusTolerant("""{"ip":"192.16"""))
    }

    @Test
    fun formatUptime() {
        assertEquals("0s", formatUptime(0))
        assertEquals("5s", formatUptime(5))
        assertEquals("1m 5s", formatUptime(65))
        assertEquals("2h 1m 5s", formatUptime(7265))
    }
}
