package zhiqiu.car.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 轻量图标徽章：用单字/符号配合品牌色底，替代对 material-icons-extended 的依赖。
 */
@Composable
fun Glyph(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 36,
    background: Color = AccentCyan.copy(alpha = 0.15f),
    contentColor: Color = AccentCyan,
    fontSize: Int = 18,
    shape: Shape = RoundedCornerShape(10.dp),
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
