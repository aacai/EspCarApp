package zhiqiu.car.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zhiqiu.car.app.ble.CarController
import zhiqiu.car.app.ble.ConnectionState
import zhiqiu.car.app.ble.KableBleClient
import zhiqiu.car.app.ui.control.ControlScreen
import zhiqiu.car.app.ui.scan.ScanScreen
import zhiqiu.car.app.ui.unsupported.UnsupportedScreen
import zhiqiu.car.app.ui.theme.AppTheme

@Composable
fun App() {
    val settings = remember { SettingsRepository() }
    val darkMode by settings.darkMode.collectAsStateWithLifecycle()
    val language by settings.language.collectAsStateWithLifecycle()
    val controller = remember { CarController(KableBleClient()) }

    AppTheme(darkTheme = darkMode) {
        // 语言切换通过修改平台 Locale 实现；key(language) 强制子树重组，
        // 使 stringResource 以新 Locale 重新解析文案并即时生效。
        key(language) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (!controller.isSupported) {
                    UnsupportedScreen(controller)
                } else {
                    val connection by controller.connectionState.collectAsStateWithLifecycle()
                    when (connection) {
                        ConnectionState.Connected -> ControlScreen(controller, settings)
                        else -> ScanScreen(controller, settings)
                    }
                }
            }
        }
    }
}
