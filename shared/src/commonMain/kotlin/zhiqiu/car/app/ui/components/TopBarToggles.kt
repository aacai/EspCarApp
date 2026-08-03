package zhiqiu.car.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import espcarclient.shared.generated.resources.Res
import espcarclient.shared.generated.resources.lang_en
import espcarclient.shared.generated.resources.lang_system
import espcarclient.shared.generated.resources.lang_zh
import org.jetbrains.compose.resources.stringResource
import zhiqiu.car.app.SettingsRepository

/**
 * 顶栏右侧的语言 / 主题切换控件。
 * 语言按钮在 跟随系统 → 中文 → English 之间循环。
 */
@Composable
fun TopBarToggles(settings: SettingsRepository, modifier: Modifier = Modifier) {
    val language by settings.language.collectAsStateWithLifecycle()
    val darkMode by settings.darkMode.collectAsStateWithLifecycle()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = {
            val next = when (language) {
                SettingsRepository.LANGUAGE_SYSTEM -> SettingsRepository.LANGUAGE_ZH
                SettingsRepository.LANGUAGE_ZH -> SettingsRepository.LANGUAGE_EN
                else -> SettingsRepository.LANGUAGE_SYSTEM
            }
            settings.setLanguage(next)
        }) {
            val label = when (language) {
                SettingsRepository.LANGUAGE_ZH -> stringResource(Res.string.lang_zh)
                SettingsRepository.LANGUAGE_EN -> stringResource(Res.string.lang_en)
                else -> stringResource(Res.string.lang_system)
            }
            Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }

        IconButton(onClick = { settings.setDarkMode(!darkMode) }) {
            if (darkMode) {
                SunGlyph(tint = Color.White, size = 22)
            } else {
                MoonGlyph(tint = Color.White, size = 22)
            }
        }
    }
}
