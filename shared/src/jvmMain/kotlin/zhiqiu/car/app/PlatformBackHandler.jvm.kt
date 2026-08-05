package zhiqiu.car.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.awt.ComposeWindow
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * Desktop（JVM）实现：桌面环境没有「系统返回键」，以 Esc 键作为返回语义。
 *
 * 注意：不能用 `Modifier.onKeyEvent` 挂在一个 Box 上——Compose 键盘事件只派发给持有焦点的
 * 节点，普通 Box 拿不到焦点，Esc 永远不会触发。这里直接找到应用的主窗口（[ComposeWindow]）
 * 在窗口上注册 AWT 键盘监听器，从窗口级捕获全局 Esc，与当前焦点位置无关。
 *
 * 新版本 Compose 将 `LocalWindow` 改为 internal，无法再直接取当前窗口，改为枚举 AWT 已创建的
 * 窗口并过滤 [ComposeWindow]；对话框是独立的 AWT 窗口，不会命中这里，因此 Esc 语义保持不变。
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose {}

        val window = Window.getWindows().firstOrNull { it is ComposeWindow && it.isShowing }
            ?: return@DisposableEffect onDispose {}
        val listener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    currentOnBack()
                }
            }
        }
        window.addKeyListener(listener)
        onDispose { window.removeKeyListener(listener) }
    }
}
