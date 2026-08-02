package zhiqiu.car.app.i18n

// Web 端语言由浏览器/系统决定，运行时无法可靠覆盖，故此处为兼容实现（设置可持久化，但文案不切换）。
actual fun setLanguage(languageCode: String?): Boolean = false

actual fun getCurrentLanguage(): String? = null
