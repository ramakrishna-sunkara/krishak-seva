package com.kisanalert.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import com.kisanalert.core.ui.components.KisanAppIcon
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.kisanalert.core.ui.components.KisanTopAppBar
import androidx.compose.runtime.Composable
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.core.ui.components.KisanOutlinedButton
import com.kisanalert.core.ui.theme.KisanAlertTheme
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.core.ui.components.LanguagePreferenceSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as Activity
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(SettingsEvent.NotificationPermissionResult(isGranted))
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.onEvent(SettingsEvent.NotificationPermissionResult(isGranted))
        } else {
            viewModel.onEvent(SettingsEvent.NotificationPermissionResult(isGranted = true))
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onEvent(SettingsEvent.DismissError)
        }
    }
    LaunchedEffect(uiState.shouldRecreateActivity) {
        if (uiState.shouldRecreateActivity) {
            viewModel.onEvent(SettingsEvent.RecreateHandled)
            activity.recreate()
        }
    }
    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            onNavigateToAuth()
        }
    }
    if (uiState.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsEvent.SignOutDismissed) },
            title = { Text(text = stringResource(R.string.settings_sign_out_title)) },
            text = { Text(text = stringResource(R.string.settings_sign_out_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SettingsEvent.SignOutConfirmed) }) {
                    Text(text = stringResource(R.string.settings_sign_out_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(SettingsEvent.SignOutDismissed) }) {
                    Text(text = stringResource(R.string.settings_sign_out_cancel))
                }
            }
        )
    }
    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = stringResource(R.string.settings_title),
                showBackButton = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                uiState = uiState,
                languageOptions = viewModel.languageOptions,
                modifier = Modifier.padding(innerPadding),
                onLanguageSelected = { language ->
                    viewModel.onEvent(SettingsEvent.LanguageSelected(language))
                },
                onNotificationsEnabledChanged = { isEnabled ->
                    viewModel.onEvent(SettingsEvent.NotificationsEnabledChanged(isEnabled))
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenSystemNotificationSettings = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                onSignOutClick = { viewModel.onEvent(SettingsEvent.SignOutClicked) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    languageOptions: List<PreferredLanguage>,
    modifier: Modifier = Modifier,
    onLanguageSelected: (PreferredLanguage) -> Unit,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSectionCard(
            title = stringResource(R.string.settings_section_preferences),
            icon = Icons.Rounded.Language
        ) {
            SettingsRow(
                title = stringResource(R.string.settings_language_title),
                subtitle = stringResource(R.string.settings_language_subtitle)
            ) {
                LanguagePreferenceSection(
                    selectedLanguage = uiState.preferredLanguage,
                    languageOptions = languageOptions,
                    onLanguageSelected = onLanguageSelected,
                    isEnabled = !uiState.isUpdatingLanguage,
                    showHeader = false
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsRow(
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_subtitle)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.areNotificationsEnabled) {
                            stringResource(R.string.settings_notifications_on)
                        } else {
                            stringResource(R.string.settings_notifications_off)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = uiState.areNotificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChanged
                    )
                }
            }
            if (!uiState.hasNotificationPermission) {
                TextButton(onClick = onRequestNotificationPermission) {
                    Text(text = stringResource(R.string.settings_grant_permission))
                }
            }
            TextButton(onClick = onOpenSystemNotificationSettings) {
                Text(text = stringResource(R.string.settings_open_system_notifications))
            }
        }
        SettingsSectionCard(
            title = stringResource(R.string.settings_section_account),
            icon = Icons.AutoMirrored.Rounded.Logout
        ) {
            KisanOutlinedButton(
                text = stringResource(R.string.settings_sign_out),
                onClick = onSignOutClick,
                isLoading = uiState.isSigningOut
            )
        }
        SettingsSectionCard(
            title = stringResource(R.string.settings_section_about),
            icon = Icons.Rounded.Info
        ) {
            AboutCard(appVersion = uiState.appVersion)
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun AboutCard(appVersion: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            KisanAppIcon(
                modifier = Modifier.size(48.dp),
                tint = KisanColors.CardGradientStart
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.settings_version, appVersion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.settings_about_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    KisanAlertTheme {
        SettingsContent(
            uiState = SettingsUiState(
                isLoading = false,
                preferredLanguage = PreferredLanguage.TELUGU,
                areNotificationsEnabled = true,
                appVersion = "1.0.0"
            ),
            languageOptions = PreferredLanguage.entries,
            onLanguageSelected = {},
            onNotificationsEnabledChanged = {},
            onRequestNotificationPermission = {},
            onOpenSystemNotificationSettings = {},
            onSignOutClick = {}
        )
    }
}
