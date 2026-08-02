package zhiqiu.car.app

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

internal actual fun platformLog(tag: String, message: String) {
    println("[$tag] $message")
}