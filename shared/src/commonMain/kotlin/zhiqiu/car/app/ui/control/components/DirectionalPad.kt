package zhiqiu.car.app.ui.control.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zhiqiu.car.app.ble.CarDirection
import zhiqiu.car.app.ui.theme.SakuraDeep

/**
 * 方向盘：上(前)/下(后)/左/右 四个方向键，按下即发送指令、松开即停车；
 * [current] 高亮当前激活方向，指针事件由 [onPress]/[onRelease] 回调到控制器。
 */
@Composable
fun DirectionalPad(
    current: CarDirection?,
    onPress: (CarDirection) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ArrowButton("↑", CarDirection.Forward, current == CarDirection.Forward, onPress, onRelease)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ArrowButton("←", CarDirection.Left, current == CarDirection.Left, onPress, onRelease)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.DirectionsCar, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ArrowButton("→", CarDirection.Right, current == CarDirection.Right, onPress, onRelease)
        }
        ArrowButton("↓", CarDirection.Backward, current == CarDirection.Backward, onPress, onRelease)
    }
}

@Composable
private fun ArrowButton(
    label: String,
    direction: CarDirection,
    active: Boolean,
    onPress: (CarDirection) -> Unit,
    onRelease: () -> Unit,
) {
    val scale = animateFloatAsState(if (active) 1.08f else 1f)
    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale.value)
            .clip(RoundedCornerShape(24.dp))
            .background(brush = if (active) SolidColor(SakuraDeep) else SolidColor(MaterialTheme.colorScheme.surfaceVariant))
            .then(
                if (active) {
                    Modifier.border(2.dp, SakuraDeep, RoundedCornerShape(24.dp))
                } else {
                    Modifier
                },
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPress(direction)
                    awaitRelease()
                    onRelease()
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 34.sp,
        )
    }
}