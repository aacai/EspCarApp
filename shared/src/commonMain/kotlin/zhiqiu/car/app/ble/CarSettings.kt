package zhiqiu.car.app.ble

/** 持久化偏好：记住上次连接设备、看门狗开关、自动重连开关。 */
public class CarSettings(private val store: KeyValueStore = createKeyValueStore()) {

    public var lastDeviceId: String?
        get() = store.getString(KEY_LAST_ID)
        set(value) {
            if (value == null) store.remove(KEY_LAST_ID) else store.putString(KEY_LAST_ID, value)
        }

    public var lastDeviceName: String?
        get() = store.getString(KEY_LAST_NAME)
        set(value) {
            if (value == null) store.remove(KEY_LAST_NAME) else store.putString(KEY_LAST_NAME, value)
        }

    /** 看门狗（安全兜底）开关，默认开启。 */
    public var watchdogEnabled: Boolean
        get() = store.getBoolean(KEY_WATCHDOG, true)
        set(value) = store.putBoolean(KEY_WATCHDOG, value)

    /** 进入 App 是否自动重连上次设备。 */
    public var autoReconnect: Boolean
        get() = store.getBoolean(KEY_AUTORECONNECT, true)
        set(value) = store.putBoolean(KEY_AUTORECONNECT, value)

    public fun rememberDevice(id: String, name: String?) {
        lastDeviceId = id
        lastDeviceName = name
    }

    public fun forgetDevice() {
        lastDeviceId = null
        lastDeviceName = null
    }

    private companion object {
        const val KEY_LAST_ID = "car.last_device_id"
        const val KEY_LAST_NAME = "car.last_device_name"
        const val KEY_WATCHDOG = "car.watchdog_enabled"
        const val KEY_AUTORECONNECT = "car.auto_reconnect"
    }
}
