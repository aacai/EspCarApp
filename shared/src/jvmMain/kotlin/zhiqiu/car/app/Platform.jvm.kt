package zhiqiu.car.app

import java.awt.Desktop
import java.net.URI

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

internal actual fun platformLog(tag: String, message: String) {
    println("[$tag] $message")
}

actual fun openBluetoothSettings() {
    // JVM 桌面平台无蓝牙设置
}

actual fun openUrl(url: String) {
    try {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
            return
        }
    } catch (_: Exception) {
        // 忽略，尝试兜底
    }
    try {
        Runtime.getRuntime().exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", url))
    } catch (_: Exception) {
        // Windows 兜底也失败时静默
    }
}