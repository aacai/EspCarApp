package zhiqiu.car.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Kable/KeyValueStore 在 Android 上需要 Context，必须在构建 UI 前注入
        zhiqiu.car.app.ble.setAndroidContext(applicationContext)

        setContent {
            AndroidBlePermissionGate {
                App()
            }
        }
    }
}