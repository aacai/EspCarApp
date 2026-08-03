package zhiqiu.car.app.ble

/** 持久化偏好：记住上次连接设备、看门狗开关、自动重连开关。 */
class CarSettings(private val store: KeyValueStore = createKeyValueStore()) {

    var lastDeviceId: String?
        get() = store.getString(KEY_LAST_ID)
        set(value) {
            if (value == null) store.remove(KEY_LAST_ID) else store.putString(KEY_LAST_ID, value)
        }

    var lastDeviceName: String?
        get() = store.getString(KEY_LAST_NAME)
        set(value) {
            if (value == null) store.remove(KEY_LAST_NAME) else store.putString(KEY_LAST_NAME, value)
        }

    /** 看门狗（安全兜底）开关，默认开启。 */
    var watchdogEnabled: Boolean
        get() = store.getBoolean(KEY_WATCHDOG, true)
        set(value) = store.putBoolean(KEY_WATCHDOG, value)

    /** 进入 App 是否自动重连上次设备。 */
    var autoReconnect: Boolean
        get() = store.getBoolean(KEY_AUTORECONNECT, true)
        set(value) = store.putBoolean(KEY_AUTORECONNECT, value)

    /** 扫描过滤前缀，默认 "EspCar_"；设为空字符串表示不过滤（扫描所有设备）。 */
    var scanNamePrefix: String
        get() = store.getString(KEY_SCAN_PREFIX) ?: DEFAULT_SCAN_PREFIX
        set(value) {
            store.putString(KEY_SCAN_PREFIX, value)
        }

    /** 过滤无法获取名字的蓝牙设备（名字为 null），默认开启。 */
    var filterNamelessEnabled: Boolean
        get() = store.getBoolean(KEY_FILTER_NAMELESS, true)
        set(value) = store.putBoolean(KEY_FILTER_NAMELESS, value)

    fun rememberDevice(id: String, name: String?) {
        lastDeviceId = id
        lastDeviceName = name
    }

    fun forgetDevice() {
        lastDeviceId = null
        lastDeviceName = null
    }

    private companion object {
        const val KEY_LAST_ID = "car.last_device_id"
        const val KEY_LAST_NAME = "car.last_device_name"
        const val KEY_WATCHDOG = "car.watchdog_enabled"
        const val KEY_AUTORECONNECT = "car.auto_reconnect"
        const val KEY_SCAN_PREFIX = "car.scan_prefix"
        const val KEY_FILTER_NAMELESS = "car.filter_nameless"
        const val DEFAULT_SCAN_PREFIX = "EspCar_"
    }
}