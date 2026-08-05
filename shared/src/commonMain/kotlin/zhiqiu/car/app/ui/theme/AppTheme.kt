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
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AccentCyan = Color(0xFF2DB5A0)
val AccentIndigo = Color(0xFF5B5EA6)
val AccentGreen = Color(0xFF8DD9BA)
val AccentAmber = Color(0xFFD96A5A)
val AccentRed = Color(0xFFD96A5A)
val SakuraPink = Color(0xFFF7B7C5)
// 较深的樱花粉，用于在浅色背景下保证文字/图标可读性
val SakuraDeep = Color(0xFFC04A68)
val SakuraDarkText = Color(0xFF3D2E2A)

val AccentGradient: Brush = Brush.linearGradient(listOf(AccentCyan, AccentIndigo))
val DangerGradient: Brush = Brush.linearGradient(listOf(Color(0xFFD96A5A), Color(0xFFCC5544)))
val HeroGradient: Brush = Brush.verticalGradient(listOf(SakuraPink, Color(0xFFF2DFD6)))
val SurfaceGradient: Brush = Brush.verticalGradient(listOf(Color(0xFFFAF8F5), Color(0xFFF5F0EB)))

private val LightColors = lightColorScheme(
    primary = SakuraPink,
    onPrimary = Color(0xFF3D2E2A),
    primaryContainer = Color(0xFFFAF8F5),
    onPrimaryContainer = Color(0xFF3D2E2A),
    secondary = Color(0xFFF2DFD6),
    onSecondary = Color(0xFF3D2E2A),
    tertiary = Color(0xFFF0DDD8),
    onTertiary = Color(0xFF3D2E2A),
    background = Color(0xFFF5F0EB),
    onBackground = Color(0xFF3D2E2A),
    surface = Color(0xFFFAF8F5),
    onSurface = Color(0xFF3D2E2A),
    surfaceVariant = Color(0xFFF0DDD8),
    onSurfaceVariant = Color(0xFF786B67),
    outline = Color(0xFFDCD9D4),
    outlineVariant = Color(0xFFDCD9D4),
    error = Color(0xFFD96A5A),
    onError = Color(0xFF3D2E2A),
    errorContainer = Color(0xFFFAF8F5),
    onErrorContainer = Color(0xFF3D2E2A),
    inverseSurface = Color(0xFF3D2E2A),
    inverseOnSurface = Color(0xFFF5F0EB),
    inversePrimary = Color(0xFFF7B7C5),
    surfaceDim = Color(0xFFF2DFD6),
    surfaceBright = Color(0xFFFAF8F5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F0EB),
    surfaceContainer = Color(0xFFF0DDD8),
    surfaceContainerHigh = Color(0xFFF2DFD6),
    surfaceContainerHighest = Color(0xFFDCD9D4),
    scrim = Color(0xFF3D2E2A).copy(alpha = 0.6f),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFAFAF8),
    onPrimary = Color(0xFF2D1E2D),
    primaryContainer = Color(0xFF362937),
    onPrimaryContainer = Color(0xFFEBE4E8),
    secondary = Color(0xFF4A4042),
    onSecondary = Color(0xFFEBE4E8),
    tertiary = Color(0xFF3D3336),
    onTertiary = Color(0xFFEBE4E8),
    background = Color(0xFF2D1E2D),
    onBackground = Color(0xFFEBE4E8),
    surface = Color(0xFF362937),
    onSurface = Color(0xFFEBE4E8),
    surfaceVariant = Color(0xFF3D3336),
    onSurfaceVariant = Color(0xFF948B8E),
    outline = Color(0xFF55484A),
    outlineVariant = Color(0xFF55484A),
    error = Color(0xFFD96A5A),
    onError = Color(0xFF2D1E2D),
    errorContainer = Color(0xFF362937),
    onErrorContainer = Color(0xFFEBE4E8),
    inverseSurface = Color(0xFFEBE4E8),
    inverseOnSurface = Color(0xFF2D1E2D),
    inversePrimary = Color(0xFFF7B7C5),
    surfaceDim = Color(0xFF2D1E2D),
    surfaceBright = Color(0xFF3D343D),
    surfaceContainerLowest = Color(0xFF241926),
    surfaceContainerLow = Color(0xFF2D1E2D),
    surfaceContainer = Color(0xFF362937),
    surfaceContainerHigh = Color(0xFF40363F),
    surfaceContainerHighest = Color(0xFF4D414B),
    scrim = Color(0xFF2D1E2D).copy(alpha = 0.6f),
)

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
                brush = if (enabled) brush else Brush.linearGradient(listOf(Color(0xFF948B8E), Color(0xFF55484A))),
                shape = RoundedCornerShape(14.dp),
            ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (enabled) MaterialTheme.colorScheme.onPrimary else Color(0xFF948B8E),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}