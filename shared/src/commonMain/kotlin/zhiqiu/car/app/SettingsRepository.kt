package zhiqiu.car.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import zhiqiu.car.app.ble.createKeyValueStore
import zhiqiu.car.app.i18n.setLanguage as platformSetLanguage

/**
 * 全局 UI 设置（主题 / 语言 / 扫描过滤），通过 KeyValueStore 跨平台持久化。
 *
 * - 主题：深色（默认）或浅色。
 * - 语言："system"（跟随系统，默认）/ "zh" / "en"。
 * - 扫描前缀：空字符串 = 不过滤；默认 "EspCar_"。
 * - 过滤无名字设备：默认开启。
 *
 * 语言切换通过 [platformSetLanguage] 修改平台 Locale 实现，Compose Resources 会据此重新解析文案。
 */
class SettingsRepository {
    private val store = createKeyValueStore()
    private val keyDarkMode = "ui.dark_mode"
    private val keyLanguage = "ui.language"
    private val keyScanPrefix = "scan.name_prefix"
    private val keyFilterNameless = "scan.filter_nameless"

    private val _darkMode = MutableStateFlow(store.getBoolean(keyDarkMode, true))
    val darkMode = _darkMode.asStateFlow()

    private val _language = MutableStateFlow(store.getString(keyLanguage) ?: LANGUAGE_SYSTEM)
    val language = _language.asStateFlow()

    private val _scanNamePrefix = MutableStateFlow(store.getString(keyScanPrefix) ?: DEFAULT_SCAN_PREFIX)
    val scanNamePrefix = _scanNamePrefix.asStateFlow()

    private val _filterNamelessEnabled = MutableStateFlow(store.getBoolean(keyFilterNameless, true))
    val filterNamelessEnabled = _filterNamelessEnabled.asStateFlow()

    init {
        platformSetLanguage(currentLocale(_language.value))
    }

    fun setDarkMode(value: Boolean) {
        _darkMode.value = value
        store.putBoolean(keyDarkMode, value)
    }

    fun setLanguage(code: String) {
        _language.value = code
        store.putString(keyLanguage, code)
        platformSetLanguage(currentLocale(code))
    }

    fun setScanNamePrefix(prefix: String) {
        // 允许为空字符串：空 = 不过滤（扫描所有设备）
        _scanNamePrefix.value = prefix
        store.putString(keyScanPrefix, prefix)
    }

    fun setFilterNamelessEnabled(enabled: Boolean) {
        _filterNamelessEnabled.value = enabled
        store.putBoolean(keyFilterNameless, enabled)
    }

    private fun currentLocale(code: String): String? = if (code == LANGUAGE_SYSTEM) null else code

    companion object {
        const val LANGUAGE_SYSTEM = "system"
        const val LANGUAGE_ZH = "zh"
        const val LANGUAGE_EN = "en"
        const val DEFAULT_SCAN_PREFIX = "EspCar_"

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
}