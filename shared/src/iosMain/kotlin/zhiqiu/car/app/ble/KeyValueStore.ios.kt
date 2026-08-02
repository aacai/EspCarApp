package zhiqiu.car.app.ble

import platform.Foundation.NSUserDefaults

internal actual fun createKeyValueStore(): KeyValueStore {
    val defaults = NSUserDefaults.standardUserDefaults
    return object : KeyValueStore {
        override fun getString(key: String): String? = defaults.stringForKey(key)
        override fun putString(key: String, value: String) = defaults.setObject(value, key)
        override fun getBoolean(key: String, default: Boolean): Boolean =
            if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)
        override fun putBoolean(key: String, value: Boolean) = defaults.setBool(value, key)
        override fun remove(key: String) = defaults.removeObjectForKey(key)
    }
}
