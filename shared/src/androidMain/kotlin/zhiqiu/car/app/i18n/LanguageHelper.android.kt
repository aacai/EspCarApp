package zhiqiu.car.app.i18n

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import zhiqiu.car.app.ble.appContext
import java.util.Locale

actual fun setLanguage(languageCode: String?): Boolean {
    val ctx = appContext ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = ctx.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
        localeManager.applicationLocales = if (languageCode == null) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList(Locale.forLanguageTag(languageCode))
        }
        return false
    }
    val locale = if (languageCode == null) Locale.getDefault() else Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(ctx.resources.configuration)
    config.setLocale(locale)
    @Suppress("DEPRECATION")
    ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
    return false
}

actual fun getCurrentLanguage(): String? {
    val ctx = appContext ?: return null
    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = ctx.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
        val list = localeManager.applicationLocales
        if (list.isEmpty) return null
        list[0]
    } else {
        Locale.getDefault()
    }
    return locale.takeIf { !it.language.isNullOrEmpty() }?.language
}
