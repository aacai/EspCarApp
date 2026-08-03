package zhiqiu.car.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp

/**
 * Desktop（JVM）实现：桌面环境没有「系统返回键」，以 Esc 键作为返回语义。
 * 挂一个零尺寸的透明容器监听键盘 Esc，拦截后触发 onBack（用 Compose 公共 KeyEvent API，
 * 不引入任何平台专属依赖）。
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (!enabled) return
    Box(
        modifier = Modifier
            .size(0.dp)
            .onKeyEvent { event ->
                if (event.key == Key.Escape) {
                    onBack()
                    true
                } else {
                    false
                }
            },
    )
}
