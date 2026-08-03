package zhiqiu.car.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import zhiqiu.car.app.ble.CarController
import zhiqiu.car.app.ble.ConnectionState
import zhiqiu.car.app.ble.KableBleClient
import zhiqiu.car.app.ui.control.ControlScreen
import zhiqiu.car.app.ui.scan.ScanScreen
import zhiqiu.car.app.ui.settings.SettingsScreen
import zhiqiu.car.app.ui.unsupported.UnsupportedScreen
import zhiqiu.car.app.ui.theme.AppTheme

@Composable
fun App() {
    val settings = remember { SettingsRepository() }
    val darkMode by settings.darkMode.collectAsStateWithLifecycle()
    val language by settings.language.collectAsStateWithLifecycle()
    val scanPrefix by settings.scanNamePrefix.collectAsStateWithLifecycle()
    val filterNamelessEnabled by settings.filterNamelessEnabled.collectAsStateWithLifecycle()
    val controller = remember { CarController(KableBleClient()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(scanPrefix) {
        controller.settings.scanNamePrefix = scanPrefix
    }
    LaunchedEffect(filterNamelessEnabled) {
        controller.settings.filterNamelessEnabled = filterNamelessEnabled
    }

    // 自动重连：仅在 App 冷启动触发一次。若记住了上次设备且开启自动重连，
    // 则后台扫描匹配后直接连上，由 ScanScreen 的联动跳转到遥控页。
    // 注意：只触发一次，避免从遥控页返回（断开）后再进扫描页时陷入"重连→进遥控→返回→重连"死循环。
    LaunchedEffect(Unit) {
        if (controller.isSupported &&
            controller.settings.autoReconnect &&
            controller.settings.lastDeviceId != null &&
            controller.connectionState.value != ConnectionState.Connected
        ) {
            scope.launch { controller.autoReconnectIfNeeded() }
        }
    }

    AppTheme(darkTheme = darkMode) {
        key(language) {
            Surface(modifier = Modifier.fillMaxSize()) {
                // 导航栈根：支持蓝牙进入扫描页，否则进入蓝牙不可用提示页。
                // 设置页通过 push 进入栈，安卓系统返回键会先 pop 回上一页，不再直接退出 App。
                Navigator(
                    screen = if (controller.isSupported) {
                        ScanScreen(controller, settings)
                    } else {
                        UnsupportedScreen(controller)
                    },
                )
            }
        }
    }
}
