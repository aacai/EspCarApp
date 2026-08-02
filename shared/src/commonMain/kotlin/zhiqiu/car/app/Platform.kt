package zhiqiu.car.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/** 跨平台日志：Android 走 android.util.Log（tag 可见），其余平台退化为 println。 */
internal expect fun platformLog(tag: String, message: String)