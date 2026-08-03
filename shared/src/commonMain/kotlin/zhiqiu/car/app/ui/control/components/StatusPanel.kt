package zhiqiu.car.app.ui.control.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import zhiqiu.car.app.ble.CarStatus
import zhiqiu.car.app.ble.formatBytes
import zhiqiu.car.app.ble.formatUptime
import zhiqiu.car.app.ui.theme.AccentAmber
import zhiqiu.car.app.ui.theme.AccentCyan
import zhiqiu.car.app.ui.theme.AccentGreen
import zhiqiu.car.app.ui.theme.AccentIndigo
import zhiqiu.car.app.ui.theme.AccentRed
import espcarclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private data class Metric(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val tint: Color,
)

/** 小车状态面板：「图标 + 数值 + 标签」卡片网格，指标用语义化强调色。 */
@Composable
fun StatusPanel(status: CarStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.DirectionsCar, null, tint = AccentCyan)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.status_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(AccentCyan.copy(alpha = 0.35f)),
                )
            }

            Spacer(Modifier.height(16.dp))

            val bleOnline = status.ble > 0
            val metrics = listOf(
                Metric(Icons.Rounded.DirectionsCar, stringResource(Res.string.label_action), status.action, AccentCyan),
                Metric(Icons.Rounded.Speed, stringResource(Res.string.label_speed), "${status.speed}%", AccentAmber),
                Metric(Icons.Rounded.Terminal, stringResource(Res.string.label_commands), status.cmd_count.toString(), AccentGreen),
                Metric(Icons.Rounded.Schedule, stringResource(Res.string.label_uptime), formatUptime(status.uptime), AccentIndigo),
                Metric(Icons.Rounded.Memory, stringResource(Res.string.label_free_heap), formatBytes(status.free_heap), AccentCyan),
                Metric(Icons.Rounded.Bluetooth, stringResource(Res.string.label_ble), if (bleOnline) stringResource(Res.string.status_online) else stringResource(Res.string.status_offline), if (bleOnline) AccentGreen else AccentRed),
                Metric(Icons.Rounded.Wifi, stringResource(Res.string.label_wifi), status.wifiState, AccentCyan),
                Metric(Icons.Rounded.Public, stringResource(Res.string.label_ip), status.ip, AccentIndigo),
            )

            metrics.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { metric ->
                        MetricCard(metric = metric, modifier = Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                if (row !== metrics.chunked(2).last()) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(metric: Metric, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(metric.tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(metric.icon, null, tint = metric.tint)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}