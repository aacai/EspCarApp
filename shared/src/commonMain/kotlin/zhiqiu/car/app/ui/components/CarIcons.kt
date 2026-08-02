package zhiqiu.car.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 一组自绘的线性矢量图标（24x24 视口，描边风格），用于取代纯文字占位图标，
 * 不依赖任何外部图标库，跨平台一致。
 */
@Composable
private fun StatIcon(
    tint: Color,
    size: Int = 22,
    render: DrawScope.(Color, Float) -> Unit,
) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val dim = minOf(this.size.width, this.size.height)
        render(tint, dim)
    }
}

@Composable
fun CarGlyph(tint: Color, size: Int = 22) = StatIcon(tint, size) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawRoundRect(c, topLeft = Offset(s * 0.08f, s * 0.42f), size = Size(s * 0.84f, s * 0.32f),
        cornerRadius = CornerRadius(s * 0.1f), style = stroke)
    drawRoundRect(c, topLeft = Offset(s * 0.3f, s * 0.26f), size = Size(s * 0.4f, s * 0.18f),
        cornerRadius = CornerRadius(s * 0.06f), style = stroke)
    drawCircle(c, center = Offset(s * 0.32f, s * 0.78f), radius = s * 0.09f, style = stroke)
    drawCircle(c, center = Offset(s * 0.68f, s * 0.78f), radius = s * 0.09f, style = stroke)
}

@Composable
fun SpeedGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawArc(c, startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(s * 0.12f, s * 0.12f), size = Size(s * 0.76f, s * 0.76f), style = stroke)
    drawLine(c, start = Offset(s * 0.5f, s * 0.5f), end = Offset(s * 0.72f, s * 0.3f), strokeWidth = sw, cap = StrokeCap.Round)
    drawCircle(c, center = Offset(s * 0.5f, s * 0.5f), radius = s * 0.05f)
}

@Composable
fun TerminalGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawRoundRect(c, topLeft = Offset(s * 0.12f, s * 0.18f), size = Size(s * 0.76f, s * 0.64f),
        cornerRadius = CornerRadius(s * 0.1f), style = stroke)
    drawLine(c, start = Offset(s * 0.3f, s * 0.4f), end = Offset(s * 0.42f, s * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.42f, s * 0.5f), end = Offset(s * 0.3f, s * 0.6f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.48f, s * 0.58f), end = Offset(s * 0.62f, s * 0.58f), strokeWidth = sw, cap = StrokeCap.Round)
}

@Composable
fun ClockGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawCircle(c, center = Offset(s * 0.5f, s * 0.5f), radius = s * 0.38f, style = stroke)
    drawLine(c, start = Offset(s * 0.5f, s * 0.5f), end = Offset(s * 0.5f, s * 0.3f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.5f, s * 0.5f), end = Offset(s * 0.66f, s * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
}

@Composable
fun MemoryGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.08f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawRoundRect(c, topLeft = Offset(s * 0.28f, s * 0.28f), size = Size(s * 0.44f, s * 0.44f),
        cornerRadius = CornerRadius(s * 0.06f), style = stroke)
    drawRoundRect(c, topLeft = Offset(s * 0.4f, s * 0.4f), size = Size(s * 0.2f, s * 0.2f),
        cornerRadius = CornerRadius(s * 0.04f), style = stroke)
    val pins = listOf(0.36f, 0.5f, 0.64f)
    pins.forEach { p ->
        drawLine(c, start = Offset(s * p, s * 0.28f), end = Offset(s * p, s * 0.18f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(c, start = Offset(s * p, s * 0.72f), end = Offset(s * p, s * 0.82f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(c, start = Offset(s * 0.28f, s * p), end = Offset(s * 0.18f, s * p), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(c, start = Offset(s * 0.72f, s * p), end = Offset(s * 0.82f, s * p), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun BluetoothGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val path = Path().apply {
        moveTo(s * 0.5f, s * 0.1f)
        lineTo(s * 0.3f, s * 0.5f)
        lineTo(s * 0.5f, s * 0.5f)
        lineTo(s * 0.3f, s * 0.9f)
        moveTo(s * 0.5f, s * 0.1f)
        lineTo(s * 0.7f, s * 0.5f)
        lineTo(s * 0.5f, s * 0.5f)
        lineTo(s * 0.7f, s * 0.9f)
    }
    drawPath(path, c, style = stroke)
}

@Composable
fun WifiGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    val cx = s * 0.5f
    val cy = s * 0.78f
    listOf(s * 0.2f, s * 0.33f, s * 0.46f).forEach { r ->
        drawArc(c, startAngle = 210f, sweepAngle = 120f, useCenter = false,
            topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2), style = stroke)
    }
    drawCircle(c, center = Offset(cx, cy), radius = s * 0.04f)
}

@Composable
fun PlayGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.08f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val path = Path().apply {
        moveTo(s * 0.36f, s * 0.24f)
        lineTo(s * 0.72f, s * 0.5f)
        lineTo(s * 0.36f, s * 0.76f)
        close()
    }
    drawPath(path, c, style = stroke)
}

@Composable
fun PauseGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.12f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.38f, s * 0.26f), end = Offset(s * 0.38f, s * 0.74f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.62f, s * 0.26f), end = Offset(s * 0.62f, s * 0.74f), strokeWidth = sw, cap = StrokeCap.Round)
}

@Composable
fun GlobeGlyph(tint: Color) = StatIcon(tint) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawCircle(c, center = Offset(s * 0.5f, s * 0.5f), radius = s * 0.38f, style = stroke)
    drawLine(c, start = Offset(s * 0.5f, s * 0.12f), end = Offset(s * 0.5f, s * 0.88f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(c, start = Offset(s * 0.12f, s * 0.5f), end = Offset(s * 0.88f, s * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
    drawArc(c, startAngle = 270f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(s * 0.32f, s * 0.12f), size = Size(s * 0.36f, s * 0.76f), style = stroke)
}

@Composable
fun SunGlyph(tint: Color, size: Int = 22) = StatIcon(tint, size) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawCircle(c, center = Offset(s * 0.5f, s * 0.5f), radius = s * 0.18f, style = stroke)
    repeat(8) { i ->
        val a = (i * 360f / 8) * PI / 180f
        val r1 = s * 0.3f
        val r2 = s * 0.42f
        drawLine(
            c,
            start = Offset(s * 0.5f + r1 * cos(a).toFloat(), s * 0.5f + r1 * sin(a).toFloat()),
            end = Offset(s * 0.5f + r2 * cos(a).toFloat(), s * 0.5f + r2 * sin(a).toFloat()),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun MoonGlyph(tint: Color, size: Int = 22) = StatIcon(tint, size) { c, s ->
    val sw = s * 0.09f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawArc(c, startAngle = 90f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(s * 0.26f, s * 0.2f), size = Size(s * 0.48f, s * 0.6f), style = stroke)
    drawArc(c, startAngle = 90f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(s * 0.4f, s * 0.2f), size = Size(s * 0.48f, s * 0.6f), style = stroke)
}
