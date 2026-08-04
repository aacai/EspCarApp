package zhiqiu.car.app.ui.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import zhiqiu.car.app.PlatformBackHandler
import zhiqiu.car.app.SettingsRepository
import zhiqiu.car.app.ui.theme.HeroGradient
import espcarclient.shared.generated.resources.Res
import espcarclient.shared.generated.resources.settings_title
import espcarclient.shared.generated.resources.settings_group_scan
import espcarclient.shared.generated.resources.settings_scan_prefix_label
import espcarclient.shared.generated.resources.settings_scan_prefix_hint
import espcarclient.shared.generated.resources.settings_filter_nameless
import espcarclient.shared.generated.resources.settings_filter_nameless_desc
import espcarclient.shared.generated.resources.settings_group_appearance
import espcarclient.shared.generated.resources.settings_theme_label
import espcarclient.shared.generated.resources.settings_language_label
import espcarclient.shared.generated.resources.settings_theme_light
import espcarclient.shared.generated.resources.settings_theme_dark
import espcarclient.shared.generated.resources.lang_system
import espcarclient.shared.generated.resources.lang_zh
import espcarclient.shared.generated.resources.lang_en
import espcarclient.shared.generated.resources.settings_save
import espcarclient.shared.generated.resources.settings_reset_default
import espcarclient.shared.generated.resources.settings_done
import espcarclient.shared.generated.resources.settings_group_about
import espcarclient.shared.generated.resources.settings_about_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
class SettingsScreen(
    private val settings: SettingsRepository,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { SettingsScreenModel(settings) }
        val keyboardController = LocalSoftwareKeyboardController.current

        // 统一返回键监听：安卓系统返回键 / 桌面 Esc / 其它平台由导航栈处理。
        // 设置页是栈内页面，优先 pop 回上一页（扫描/控制页），而不是直接退出 App。
        PlatformBackHandler(enabled = true) { navigator.pop() }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(HeroGradient)) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    title = {
                        Text(
                            stringResource(Res.string.settings_title),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ---- 扫描过滤 ----
            item {
                Text(
                    text = stringResource(Res.string.settings_group_scan),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = viewModel.prefixInput.value,
                        onValueChange = { viewModel.onPrefixChange(it) },
                        label = { Text(stringResource(Res.string.settings_scan_prefix_label)) },
                        placeholder = { Text(stringResource(Res.string.settings_scan_prefix_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = { viewModel.resetPrefix() },
                        ) {
                            Text(stringResource(Res.string.settings_reset_default))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.savePrefix()
                                keyboardController?.hide()
                            },
                            enabled = viewModel.prefixDirty,
                        ) {
                            Text(stringResource(Res.string.settings_save))
                        }
                    }
                }
            }

            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.settings_filter_nameless))
                    },
                    supportingContent = {
                        Text(stringResource(Res.string.settings_filter_nameless_desc))
                    },
                    trailingContent = {
                        Switch(
                            checked = viewModel.filterNamelessEnabled,
                            onCheckedChange = { settings.setFilterNamelessEnabled(it) },
                        )
                    },
                    colors = ListItemDefaults.colors(),
                )
            }

            // ---- 外观 ----
            item {
                Text(
                    text = stringResource(Res.string.settings_group_appearance),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.settings_theme_label))
                    },
                    supportingContent = {
                        Text(
                            if (viewModel.darkMode) stringResource(Res.string.settings_theme_dark)
                            else stringResource(Res.string.settings_theme_light)
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Rounded.Palette, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(),
                    modifier = Modifier.clickable { viewModel.openThemeDialog() },
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.settings_language_label))
                    },
                    supportingContent = {
                        Text(
                            when (viewModel.language) {
                                SettingsRepository.LANGUAGE_SYSTEM -> stringResource(Res.string.lang_system)
                                SettingsRepository.LANGUAGE_ZH -> stringResource(Res.string.lang_zh)
                                else -> stringResource(Res.string.lang_en)
                            }
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Rounded.Translate, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(),
                    modifier = Modifier.clickable { viewModel.openLanguageDialog() },
                )
            }

            // ---- 关于 ----
            item {
                Text(
                    text = stringResource(Res.string.settings_group_about),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.settings_about_label))
                    },
                    leadingContent = {
                        Icon(Icons.Rounded.Info, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(),
                    modifier = Modifier.clickable { viewModel.openAboutDialog() },
                )
            }
        }
    }

    if (viewModel.showThemeDialog.value) {
        SelectionDialog(
            title = stringResource(Res.string.settings_theme_label),
            onDismiss = { viewModel.dismissThemeDialog() },
        ) {
            SelectionOption(
                selected = !viewModel.darkMode,
                label = stringResource(Res.string.settings_theme_light),
                onClick = {
                    viewModel.setTheme(false)
                },
            )
            SelectionOption(
                selected = viewModel.darkMode,
                label = stringResource(Res.string.settings_theme_dark),
                onClick = {
                    viewModel.setTheme(true)
                },
            )
        }
    }

    if (viewModel.showLanguageDialog.value) {
        SelectionDialog(
            title = stringResource(Res.string.settings_language_label),
            onDismiss = { viewModel.dismissLanguageDialog() },
        ) {
            SelectionOption(
                selected = viewModel.language == SettingsRepository.LANGUAGE_SYSTEM,
                label = stringResource(Res.string.lang_system),
                onClick = {
                    viewModel.setLanguage(SettingsRepository.LANGUAGE_SYSTEM)
                },
            )
            SelectionOption(
                selected = viewModel.language == SettingsRepository.LANGUAGE_ZH,
                label = stringResource(Res.string.lang_zh),
                onClick = {
                    viewModel.setLanguage(SettingsRepository.LANGUAGE_ZH)
                },
            )
            SelectionOption(
                selected = viewModel.language == SettingsRepository.LANGUAGE_EN,
                label = stringResource(Res.string.lang_en),
                onClick = {
                    viewModel.setLanguage(SettingsRepository.LANGUAGE_EN)
                },
            )
        }
    }

    if (viewModel.showAboutDialog.value) {
        AboutDialog(onDismiss = { viewModel.dismissAboutDialog() })
    }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    options: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                options()
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_done))
            }
        },
    )
}

@Composable
private fun SelectionOption(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
