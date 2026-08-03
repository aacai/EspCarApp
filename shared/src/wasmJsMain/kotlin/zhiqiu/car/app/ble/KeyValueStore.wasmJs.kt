@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package zhiqiu.car.app.ble

import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.unsafeCast

// 直接用 wasmJs 的 JS 互操作声明浏览器 localStorage，避免依赖 kotlinx-browser
//（其在 wasmJs 上的变体无法被 Gradle 正确解析）。
private external interface JsStorage : JsAny {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

private val rawStorage: JsAny = js("localStorage")
private val storage = rawStorage.unsafeCast<JsStorage>()

// Web 端使用浏览器 localStorage 做持久化，刷新页面后设置（看门狗开关、上次连接设备）仍然保留。
// macOS / Android / iOS 均为对应平台的真实持久化存储。
internal actual fun createKeyValueStore(): KeyValueStore =
    object : KeyValueStore {
        override fun getString(key: String): String? = storage.getItem(key)
        override fun putString(key: String, value: String) = storage.setItem(key, value)
        override fun getBoolean(key: String, default: Boolean): Boolean =
            storage.getItem(key)?.toBooleanStrictOrNull() ?: default
        override fun putBoolean(key: String, value: Boolean) = storage.setItem(key, value.toString())
        override fun remove(key: String) = storage.removeItem(key)
    }
