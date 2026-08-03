package zhiqiu.car.app

import androidx.compose.runtime.Composable

/**
 * Web（Wasm/JS）实现：无操作。
 * 浏览器的后退按钮属于宿主环境，Compose 层不拦截；导航返回由 Voyager 的导航栈管理。
 * 若需要拦截浏览器后退，可在平台入口处监听 history popstate 自行处理。
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // no-op on Web
}
