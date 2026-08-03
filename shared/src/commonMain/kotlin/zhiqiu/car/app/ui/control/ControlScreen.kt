package zhiqiu.car.app.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import zhiqiu.car.app.ble.CarController
import zhiqiu.car.app.ble.ConnectionState
import zhiqiu.car.app.PlatformBackHandler
import zhiqiu.car.app.SettingsRepository
import zhiqiu.car.app.ui.scan.ScanScreen
import zhiqiu.car.app.ui.settings.SettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import zhiqiu.car.app.ui.control.components.DirectionalPad
import zhiqiu.car.app.ui.control.components.StatusPanel
import zhiqiu.car.app.ui.theme.AccentAmber
import zhiqiu.car.app.ui.theme.AccentCyan
import zhiqiu.car.app.ui.theme.AccentGreen
import zhiqiu.car.app.ui.theme.AccentRed
import zhiqiu.car.app.ui.theme.HeroGradient
import zhiqiu.car.app.ui.theme.SakuraDeep
import espcarclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * 控制页：连接状态胶囊 + 方向盘 + 速度调节 + 实时状态面板 + 断开连接。
 * 安全兜底（松手即停、看门狗、断连复位）已在 [CarController] 内部实现。
 */
class ControlScreen(
    private val controller: CarController,
    private val settings: SettingsRepository,
) : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val status by controller.status.collectAsStateWithLifecycle()
        val statusError by controller.statusError.collectAsStateWithLifecycle()
        val currentDirection by controller.currentDirection.collectAsStateWithLifecycle()
        val connection by controller.connectionState.collectAsStateWithLifecycle()
        val error by controller.error.collectAsStateWithLifecycle()
        val logLines by controller.logLines.collectAsStateWithLifecycle()
        val scope = rememberCoroutineScope()

        var speed by remember { mutableStateOf(60) }
        var showLog by remember { mutableStateOf(false) }

        // 统一返回键监听：安卓系统返回键 / 桌面 Esc 等，拦截为「断开并回到扫描页」，
        // 而不是直接退出 App（也不会因保留连接而停在怪异的已连接扫描态）。
        PlatformBackHandler(enabled = true) { scope.launch { controller.disconnect() } }

        // 断连后回到扫描页（replace 避免返回时又跳回遥控页）。
        LaunchedEffect(connection) {
            if (connection == ConnectionState.Disconnected) {
                navigator.replace(ScanScreen(controller, settings))
            }
        }

        Scaffold(
        topBar = {
            Box(modifier = Modifier.background(HeroGradient)) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.DirectionsCar, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            Text(
                                stringResource(Res.string.control_topbar_title),
                                modifier = Modifier.padding(start = 10.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { controller.disconnect() } }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navigator.push(SettingsScreen(controller, settings)) }) {
                            Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        TextButton(onClick = { showLog = !showLog }) {
                            Text(stringResource(Res.string.debug), color = AccentCyan)
                        }
                        ConnectionChip(connection)
                        TextButton(
                            onClick = { scope.launch { controller.disconnect() } },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(stringResource(Res.string.disconnect), color = AccentRed)
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                ) {
                    Text(
                        text = error ?: "",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            DirectionalPad(
                current = currentDirection,
                onPress = { controller.pressDirection(it) },
                onRelease = { controller.releaseDirection() },
            )

            SpeedCard(
                speed = speed,
                onSpeedChange = {
                    speed = it
                    controller.setSpeed(it)
                },
            )

            Button(
                onClick = { scope.launch { controller.emergencyStop() } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(
                    stringResource(Res.string.emergency_stop),
                    modifier = Modifier.padding(vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (status != null) {
                StatusPanel(status = status!!)
                if (statusError != null) {
                    Text(
                        statusError!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = AccentAmber,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                Text(
                    statusError ?: stringResource(Res.string.fetching_status),
                    color = if (statusError != null) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (showLog) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                    ) {
                        Text(
                            stringResource(Res.string.debug_log_title),
                            color = AccentCyan,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        val text = if (logLines.isEmpty()) stringResource(Res.string.no_log) else logLines.joinToString("\n")
                        Text(
                            text = text,
                            color = Color(0xFF9EFFA0),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
    }
}

/** 顶栏连接状态胶囊：彩色圆点 + 文字。 */
@Composable
private fun ConnectionChip(connection: ConnectionState) {
    val (color, text) = when (connection) {
        ConnectionState.Connected -> AccentGreen to stringResource(Res.string.connected)
        ConnectionState.Connecting -> AccentAmber to stringResource(Res.string.connecting)
        else -> AccentRed to stringResource(Res.string.disconnected)
    }
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text,
                modifier = Modifier.padding(start = 6.dp),
                color = color,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/** 速度卡片：圆形进度环 + 滑块。 */
@Composable
private fun SpeedCard(speed: Int, onSpeedChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { speed / 100f },
                    modifier = Modifier.size(64.dp),
                    color = SakuraDeep,
                    trackColor = MaterialTheme.colorScheme.outline,
                    strokeWidth = 6.dp,
                )
                Text("$speed%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(stringResource(Res.string.speed_label), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Slider(
                    value = speed.toFloat(),
                    onValueChange = { onSpeedChange(it.toInt()) },
                    valueRange = 0f..100f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = SakuraDeep,
                        activeTrackColor = SakuraDeep,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }
    }
}