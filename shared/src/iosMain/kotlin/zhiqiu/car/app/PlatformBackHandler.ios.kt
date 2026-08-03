package zhiqiu.car.app

import androidx.compose.runtime.Composable

/**
 * iOS 实现：无操作。
 * iOS 的「返回」来自 UIKit 的导航栈滑动手势，由 Voyager 的导航栈自行管理，
 * Compose 层无法也不应拦截系统级手势，故此处为空。如需自定义，可在 Swift/UIKit 侧处理。
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // no-op on iOS
}
