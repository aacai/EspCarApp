package zhiqiu.car.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Android 蓝牙权限闸门：在显示业务页面之前，确保已授予 BLE 所需权限。
 * - Android 12+ (API 31+)：BLUETOOTH_SCAN（neverForLocation）、BLUETOOTH_CONNECT
 * - Android 11- (API <31)：BLUETOOTH、BLUETOOTH_ADMIN、ACCESS_FINE_LOCATION
 * 权限未授予时展示说明与「授予权限」按钮，避免 Kable 扫描因缺权限而静默失败。
 */
@Composable
fun AndroidBlePermissionGate(content: @Composable () -> Unit) {
    var granted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results -> granted = results.values.all { it } }

    LaunchedEffect(Unit) { launcher.launch(requiredBlePermissions()) }

    if (granted) {
        content()
    } else {
        PermissionRationale(onRequest = { launcher.launch(requiredBlePermissions()) })
    }
}

private fun requiredBlePermissions(): Array<String> = if (Build.VERSION.SDK_INT >= 31) {
    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("需要蓝牙权限", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "本应用通过蓝牙搜索并控制小车，需要授予蓝牙相关权限后才能使用。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRequest) { Text("授予权限") }
        }
    }
}
