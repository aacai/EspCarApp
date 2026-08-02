package zhiqiu.car.app.ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import zhiqiu.car.app.ble.CarController
import zhiqiu.car.app.ble.ConnectionState
import zhiqiu.car.app.ble.DiscoveredDevice
import zhiqiu.car.app.SettingsRepository
import zhiqiu.car.app.ui.components.TopBarToggles
import zhiqiu.car.app.ui.theme.AccentCyan
import zhiqiu.car.app.ui.theme.AccentGradient
import zhiqiu.car.app.ui.components.BluetoothGlyph
import zhiqiu.car.app.ui.components.CarGlyph
import zhiqiu.car.app.ui.components.PauseGlyph
import zhiqiu.car.app.ui.components.PlayGlyph
import zhiqiu.car.app.ui.theme.GradientButton
import zhiqiu.car.app.ui.theme.HeroGradient
import espcarclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * 扫描页：列出周围 `EspCar_` 前缀的设备，点击即可连接。
 * 进入后自动开始扫描；用户可手动暂停/继续。若已记住上次设备并开启自动重连，
 * 会在后台匹配到后直接连上并跳转到控制页。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(controller: CarController, settings: SettingsRepository) {
    val devices by controller.scanDevices.collectAsState()
    val isScanning by controller.isScanning.collectAsState()
    val error by controller.error.collectAsState()
    val connection by controller.connectionState.collectAsState()
    // 扫描限时自动停止：BLE 持续扫描很费电，进入页面只扫一次，超时即停。
    val scanTimeoutMs = 10_000L
    var userStopped by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        if (connection != ConnectionState.Connected) {
            userStopped = false
            controller.startScan(scanTimeoutMs)
        }
        onDispose {
            controller.stopScan()
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
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BluetoothGlyph(tint = Color.White)
                            }
                            Text(
                                stringResource(Res.string.scan_topbar_title),
                                modifier = Modifier.padding(start = 10.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    },
                    actions = {
                        TopBarToggles(settings)
                        IconButton(onClick = {
                            if (isScanning) {
                                userStopped = true
                                controller.stopScan()
                            } else {
                                userStopped = false
                                controller.startScan(scanTimeoutMs)
                            }
                        }) {
                            if (isScanning) PauseGlyph(tint = Color.White) else PlayGlyph(tint = Color.White)
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
        ) {
            ScanHero(isScanning = isScanning, count = devices.size, autoStopped = !isScanning && !userStopped)

            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    ),
                ) {
                    Text(
                        text = error ?: "",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (devices.isEmpty() && !isScanning) {
                Column(
                    modifier = Modifier.padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        BluetoothGlyph(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = stringResource(Res.string.no_devices),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.no_devices_hint),
                        modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onConnect = { controller.connectDevice(device) },
                    )
                }
            }
        }
    }
}

/** 扫描英雄区：渐变卡片 + 脉冲雷达盘 + 实时计数。 */
@Composable
private fun ScanHero(isScanning: Boolean, count: Int, autoStopped: Boolean) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = InfiniteRepeatableSpec(tween(900), RepeatMode.Reverse),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroGradient)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = pulse)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        CarGlyph(tint = Color.White)
                    }
                }
                Text(
                    text = when {
                        isScanning -> stringResource(Res.string.scanning)
                        autoStopped -> stringResource(Res.string.scan_auto_stopped)
                        else -> stringResource(Res.string.scan_paused)
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.scan_count, count),
                    modifier = Modifier.padding(top = 4.dp),
                    color = AccentCyan,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** 设备卡片：图标 + 名称/地址/信号强度 + 渐变连接按钮。 */
@Composable
private fun DeviceCard(device: DiscoveredDevice, onConnect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConnect),
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
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                CarGlyph(tint = MaterialTheme.colorScheme.primary)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    device.name ?: stringResource(Res.string.unknown_device),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SignalBars(device.rssi)
                    Text(
                        stringResource(Res.string.device_id, device.id),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            GradientButton(
                text = stringResource(Res.string.connect),
                onClick = onConnect,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** 信号强度条：根据 RSSI 渲染 0~4 格。 */
@Composable
private fun SignalBars(rssi: Int?) {
    val level = when {
        rssi == null -> 0
        rssi >= -50 -> 4
        rssi >= -60 -> 3
        rssi >= -70 -> 2
        rssi >= -80 -> 1
        else -> 0
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        for (i in 1..4) {
            val active = i <= level
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = (6 + i * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) {
                            AccentCyan
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    ),
            )
        }
    }
}
