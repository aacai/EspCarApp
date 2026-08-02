package zhiqiu.car.app.i18n

import java.util.Locale

private val systemDefaultLocale = Locale.getDefault()

actual fun setLanguage(languageCode: String?): Boolean {
    Locale.setDefault(if (languageCode == null) systemDefaultLocale else Locale.forLanguageTag(languageCode))
    return false
}

actual fun getCurrentLanguage(): String? {
    val locale = Locale.getDefault()
    return locale.takeIf { !it.language.isNullOrEmpty() }?.language
}
