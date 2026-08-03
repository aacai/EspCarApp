package zhiqiu.car.app.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.viewmodel.ScreenModel
import zhiqiu.car.app.SettingsRepository

/**
 * 设置页的 UI 状态与行为载体（Voyager ScreenModel，生命周期与设置页绑定，
 * 出栈时由 Voyager 自动清理）。
 *
 * 本地编辑态（[prefixInput]）只在进入页面时从持久化值初始化一次，
 * 保存后不会被 flow 回写覆盖；[showThemeDialog] / [showLanguageDialog] 是弹窗可见态。
 * 持久化读写全部委托给 [SettingsRepository]。
 */
class SettingsScreenModel(
    private val settings: SettingsRepository,
) : ScreenModel {
    // 持久化值（只读，供 UI 展示与"脏检查"对比）
    val darkMode get() = settings.darkMode.value
    val language get() = settings.language.value
    val scanPrefix get() = settings.scanNamePrefix.value
    val filterNamelessEnabled get() = settings.filterNamelessEnabled.value

    // 本地编辑态
    private val _prefixInput = mutableStateOf(settings.scanNamePrefix.value)
    val prefixInput: State<String> = _prefixInput

    private val _showThemeDialog = mutableStateOf(false)
    val showThemeDialog: State<Boolean> = _showThemeDialog

    private val _showLanguageDialog = mutableStateOf(false)
    val showLanguageDialog: State<Boolean> = _showLanguageDialog

    /** 输入是否与持久化值不同（用于启用"保存"按钮）。 */
    val prefixDirty: Boolean
        get() = _prefixInput.value.trim() != settings.scanNamePrefix.value.trim()

    fun onPrefixChange(value: String) {
        _prefixInput.value = value
    }

    fun resetPrefix() {
        _prefixInput.value = SettingsRepository.DEFAULT_SCAN_PREFIX
    }

    fun savePrefix() {
        settings.setScanNamePrefix(_prefixInput.value)
        // 保存后同步本地态，避免被 flow 回写抖动
        _prefixInput.value = settings.scanNamePrefix.value
    }

    fun setTheme(dark: Boolean) {
        settings.setDarkMode(dark)
        _showThemeDialog.value = false
    }

    fun setLanguage(code: String) {
        settings.setLanguage(code)
        _showLanguageDialog.value = false
    }

    fun setFilterNameless(enabled: Boolean) {
        settings.setFilterNamelessEnabled(enabled)
    }

    fun openThemeDialog() {
        _showThemeDialog.value = true
    }

    fun openLanguageDialog() {
        _showLanguageDialog.value = true
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
    }

    fun dismissLanguageDialog() {
        _showLanguageDialog.value = false
    }
}
