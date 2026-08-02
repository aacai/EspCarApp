package zhiqiu.car.app.i18n

/**
 * 在运行时切换应用语言（覆盖系统语言）。
 *
 * 实现上通过修改平台 Locale 完成，Compose Resources 会据此重新解析 `Res.string` 文案。
 * 返回 true 表示需要重建 Activity 才能生效（旧版 Android），false 表示已自动生效。
 */
expect fun setLanguage(languageCode: String?): Boolean

/** 获取当前生效的语言代码（如 "zh"、"en"）；未显式设置时返回 null（跟随系统）。 */
expect fun getCurrentLanguage(): String?

object LanguageCodes {
    const val ENGLISH = "en"
    const val CHINESE = "zh"
}
