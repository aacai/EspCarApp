package zhiqiu.car.app.ble

// 注：Kotlin/Wasm 的 JS 互操作对 localStorage 的调用受限（@JsFun 在当前 stdlib 不可用），
// 此处使用内存实现。代价：Web 端设置（看门狗开关、上次连接设备）在页面刷新后不持久化。
// macOS / Android / iOS 均为真实持久化存储。如需 Web 持久化，后续可接入 kotlinx-browser。
internal actual fun createKeyValueStore(): KeyValueStore {
    val map = mutableMapOf<String, String>()
    return object : KeyValueStore {
        override fun getString(key: String): String? = map[key]
        override fun putString(key: String, value: String) {
            map[key] = value
        }
        override fun getBoolean(key: String, default: Boolean): Boolean =
            map[key]?.toBooleanStrictOrNull() ?: default
        override fun putBoolean(key: String, value: Boolean) {
            map[key] = value.toString()
        }
        override fun remove(key: String) {
            map.remove(key)
        }
    }
}
