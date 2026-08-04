package zhiqiu.car.app.ui.unsupported

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import zhiqiu.car.app.ble.CarController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material3.Icon
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import zhiqiu.car.app.PlatformBackHandler
import zhiqiu.car.app.ui.theme.HeroGradient
import espcarclient.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * 蓝牙不可用提示页：移动端未授予蓝牙权限/硬件不可用时展示，并提供跳转系统蓝牙设置的入口。
 */
class UnsupportedScreen(
    private val controller: CarController,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // 蓝牙不可用页也是导航栈根页：返回键直接退出 App。
        PlatformBackHandler(enabled = true) { navigator.pop() }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(HeroGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.BluetoothDisabled, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(64.dp))
                }
                Text(
                    text = stringResource(Res.string.bluetooth_unavailable),
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(Res.string.bluetooth_unavailable_hint),
                    modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}