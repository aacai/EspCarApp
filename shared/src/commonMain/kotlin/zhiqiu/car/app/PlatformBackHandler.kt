package zhiqiu.car.app

import androidx.compose.runtime.Composable

/**
 * 跨平台「返回键」监听。
 *
 * 统一抽象各平台对「返回」的语义（Android 系统返回键、Desktop Esc、iOS 手势等），
 * 通用页面只依赖此接口，不直接引用任何平台专属 API（如 androidx.activity.compose.BackHandler）。
 *
 * @param enabled 是否拦截返回；为 false 时把返回事件交还给平台/导航栈默认行为。
 * @param onBack  拦截到返回事件时的回调。
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
