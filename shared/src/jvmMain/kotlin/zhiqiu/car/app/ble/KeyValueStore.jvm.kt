package zhiqiu.car.app.ble

import java.util.prefs.Preferences

internal actual fun createKeyValueStore(): KeyValueStore {
    val prefs = Preferences.userRoot().node("zhiqiu/car/app")
    return object : KeyValueStore {
        override fun getString(key: String): String? = prefs.get(key, null)
        override fun putString(key: String, value: String) {
            prefs.put(key, value)
            prefs.flush()
        }
        override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
        override fun putBoolean(key: String, value: Boolean) {
            prefs.putBoolean(key, value)
            prefs.flush()
        }
        override fun remove(key: String) {
            prefs.remove(key)
            prefs.flush()
        }
    }
}
