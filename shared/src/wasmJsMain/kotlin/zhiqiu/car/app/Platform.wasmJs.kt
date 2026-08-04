@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package zhiqiu.car.app

import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.unsafeCast

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

internal actual fun platformLog(tag: String, message: String) {
    println("[$tag] $message")
}

actual fun openBluetoothSettings() {
    // Web 平台无蓝牙设置
}

// 直接声明浏览器 window.open，避免依赖 kotlinx-browser（wasmJs 上无法被 Gradle 正确解析）。
private external interface JsWindow : JsAny {
    fun open(url: String, target: String): JsAny?
}

private val rawWindow: JsAny = js("window")
private val jsWindow = rawWindow.unsafeCast<JsWindow>()

actual fun openUrl(url: String) {
    jsWindow.open(url, "_blank")
}