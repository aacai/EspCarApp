package zhiqiu.car.app.ble

import android.content.Context
import android.content.SharedPreferences

internal var appContext: Context? = null

/** 由 androidApp 在 Application/Activity 创建时调用，提供 SharedPreferences 所需的 Context。 */
fun setAndroidContext(context: Context) {
    appContext = context.applicationContext
}

internal actual fun createKeyValueStore(): KeyValueStore {
    val ctx = appContext
        ?: error("Android Context 未初始化：请在使用 AppSettings 前调用 setAndroidContext()")
    val prefs: SharedPreferences = ctx.getSharedPreferences("esp_car_prefs", Context.MODE_PRIVATE)
    return object : KeyValueStore {
        override fun getString(key: String): String? = prefs.getString(key, null)
        override fun putString(key: String, value: String) {
            prefs.edit().putString(key, value).apply()
        }
        override fun getBoolean(key: String, default: Boolean): Boolean =
            prefs.getBoolean(key, default)
        override fun putBoolean(key: String, value: Boolean) {
            prefs.edit().putBoolean(key, value).apply()
        }
        override fun remove(key: String) {
            prefs.edit().remove(key).apply()
        }
    }
}
