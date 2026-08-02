package zhiqiu.car.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 品牌强调色：青蓝 → 靛蓝渐变，营造科技/仪表盘质感。 */
val AccentCyan = Color(0xFF38BDF8)
val AccentIndigo = Color(0xFF6366F1)
val AccentGreen = Color(0xFF34D399)
val AccentAmber = Color(0xFFFBBF24)
val AccentRed = Color(0xFFF87171)

/** 全局渐变笔刷，供主按钮 / 顶栏 / 卡片使用。 */
val AccentGradient: Brush = Brush.linearGradient(listOf(AccentCyan, AccentIndigo))
val DangerGradient: Brush = Brush.linearGradient(listOf(Color(0xFFF87171), Color(0xFFEF4444)))
val HeroGradient: Brush = Brush.verticalGradient(listOf(Color(0xFF0E3A5A), Color(0xFF0B1220)))
val SurfaceGradient: Brush = Brush.verticalGradient(listOf(Color(0xFF131C2E), Color(0xFF0B1220)))

private val DarkColors = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF06283D),
    primaryContainer = Color(0xFF0E3A5A),
    onPrimaryContainer = Color(0xFFBEE7FF),
    secondary = AccentCyan,
    onSecondary = Color(0xFF06283D),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF131C2E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = AccentRed,
    onError = Color(0xFF1A0606),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6ECFF),
    onPrimaryContainer = Color(0xFF03344E),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
)

/** 应用主题：默认深色（科技仪表盘观感），可按 [darkTheme] 切换为浅色。 */
@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

/** 通用渐变主按钮：透明底 + 渐变笔刷，自带圆角与点击反馈。[brush] 默认品牌渐变。 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = AccentGradient,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .background(
                brush = if (enabled) brush else Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155))),
                shape = RoundedCornerShape(14.dp),
            ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFFCBD5E1),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
