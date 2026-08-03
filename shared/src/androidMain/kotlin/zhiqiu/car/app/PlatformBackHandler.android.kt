package zhiqiu.car.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android 实现：直接委托给 androidx.activity.compose.BackHandler，
 * 它会在 ComponentActivity 的 OnBackPressedDispatcher 上注册回调，
 * 从而拦截系统返回键（物理/手势）。是 Android 平台里唯一正确的返回键拦截方式。
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
