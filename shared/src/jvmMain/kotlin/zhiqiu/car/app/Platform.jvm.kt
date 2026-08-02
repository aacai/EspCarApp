package zhiqiu.car.app

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

internal actual fun platformLog(tag: String, message: String) {
    println("[$tag] $message")
}