package zhiqiu.car.app.i18n

import platform.Foundation.NSUserDefaults

actual fun setLanguage(languageCode: String?): Boolean {
    val defaults = NSUserDefaults.standardUserDefaults
    if (languageCode == null) {
        defaults.removeObjectForKey("AppleLanguages")
    } else {
        defaults.setObject(listOf(languageCode), "AppleLanguages")
    }
    defaults.synchronize()
    return false
}

actual fun getCurrentLanguage(): String? {
    val languages = NSUserDefaults.standardUserDefaults.stringArrayForKey("AppleLanguages")
    val code = (languages?.firstOrNull() as? String) ?: return null
    return code.substringBefore('-').substringBefore('_').takeIf { it.isNotEmpty() }
}
