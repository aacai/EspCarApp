package zhiqiu.car.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/** 跨平台日志：Android 走 android.util.Log（tag 可见），其余平台退化为 println。 */
internal expect fun platformLog(tag: String, message: String)

/** 打开系统蓝牙设置页面。Android 跳转系统设置，其余平台为空操作。 */
expect fun openBluetoothSettings()