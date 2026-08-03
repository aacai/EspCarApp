package zhiqiu.car.app.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import zhiqiu.car.app.ble.CarController
import zhiqiu.car.app.ble.ConnectionState
import zhiqiu.car.app.ble.DiscoveredDevice
import zhiqiu.car.app.openBluetoothSettings
import zhiqiu.car.app.SettingsRepository
import zhiqiu.car.app.ui.control.ControlScreen
import zhiqiu.car.app.ui.settings.SettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import zhiqiu.car.app.ui.theme.AccentCyan
import zhiqiu.car.app.ui.theme.HeroGradient
import zhiqiu.car.app.ui.theme.SakuraDeep
import zhiqiu.car.app.ui.theme.SakuraPink
import espcarclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * 扫描页：列出周围 `EspCar_` 前缀的设备，点击即可连接。
 * 进入后自动开始扫描；用户可手动暂停/继续。若已记住上次设备并开启自动重连，
 * 会在后台匹配到后直接连上并跳转到控制页。
 */
class ScanScreen(
    private val controller: CarController,
    private val settings: SettingsRepository,
) : Screen {
    override val key = uniqueScreenKey

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val devices by controller.scanDevices.collectAsStateWithLifecycle()
        val isScanning by controller.isScanning.collectAsStateWithLifecycle()
        val error by controller.error.collectAsStateWithLifecycle()
        val connection by controller.connectionState.collectAsStateWithLifecycle()
        // 扫描限时自动停止：BLE 持续扫描很费电，进入页面只扫一次，超时即停。
        val scanTimeoutMs = 10_000L
        var userStopped by remember { mutableStateOf(false) }
        var connectingDeviceId by remember { mutableStateOf<String?>(null) }
        // 防止回到扫描页后（仍保持连接）再次自动跳进控制页造成死循环。
        var navigatedToControl by remember { mutableStateOf(false) }

        // 连接成功后进入控制页。用 push 保留扫描页在栈底，使系统返回键能回到扫描页；
        // navigatedToControl 标志保证 pop 回扫描页后不会重复跳转。
        LaunchedEffect(connection) {
            if (connection == ConnectionState.Connected && !navigatedToControl) {
                navigatedToControl = true
                navigator.push(ControlScreen(controller, settings))
            }
        }

        DisposableEffect(Unit) {
            if (connection != ConnectionState.Connected) {
                userStopped = false
                // 若 App 冷启动的自动重连已在后台扫描（isScanning=true），则不重复 startScan，
                // 避免两套扫描逻辑相互干扰；否则正常开始一次限时扫描。
                if (!controller.isScanning.value) {
                    controller.startScan(scanTimeoutMs)
                }
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
                            Text(
                                stringResource(Res.string.scan_topbar_title),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        actions = {
                            IconButton(onClick = { navigator.push(SettingsScreen(settings)) }) {
                                Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            IconButton(onClick = {
                                if (isScanning) {
                                    userStopped = true
                                    controller.stopScan()
                                } else {
                                    userStopped = false
                                    controller.startScan(scanTimeoutMs)
                                }
                            }) {
                                Icon(if (isScanning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary)
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
                    val isBtDisabled = error?.contains("bluetooth", ignoreCase = true) == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBtDisabled) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            },
                        ),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isBtDisabled) {
                                    stringResource(Res.string.bluetooth_disabled_hint)
                                } else {
                                    error ?: ""
                                },
                                color = if (isBtDisabled) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (isBtDisabled) {
                                Button(
                                    onClick = { openBluetoothSettings() },
                                    modifier = Modifier.padding(top = 10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    Text(stringResource(Res.string.open_bluetooth_settings))
                                }
                            }
                        }
                    }
                }

                if (devices.isEmpty() && !isScanning) {
                    Column(
                        modifier = Modifier.padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(Res.string.no_devices),
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
                            isConnecting = connection == ConnectionState.Connecting && connectingDeviceId == device.id,
                            onConnect = {
                                connectingDeviceId = device.id
                                controller.connectDevice(device)
                            },
                        )
                    }
                }
            }
        }
    }
}
/** 扫描英雄区：渐变卡片 + M3 旋转指示器 + 实时计数。 */
@Composable
private fun ScanHero(isScanning: Boolean, count: Int, autoStopped: Boolean) {
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
                if (isScanning) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
                Text(
                    text = when {
                        isScanning -> stringResource(Res.string.scanning)
                        autoStopped -> stringResource(Res.string.scan_auto_stopped)
                        else -> stringResource(Res.string.scan_paused)
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
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
private fun DeviceCard(device: DiscoveredDevice, isConnecting: Boolean, onConnect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isConnecting) Modifier else Modifier.clickable(onClick = onConnect)),
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
                    .background(SakuraPink.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Bluetooth, null, tint = SakuraDeep)
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
            Button(
                onClick = onConnect,
                modifier = Modifier.padding(start = 8.dp),
                enabled = !isConnecting,
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (isConnecting) stringResource(Res.string.connecting) else stringResource(Res.string.connect))
            }
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