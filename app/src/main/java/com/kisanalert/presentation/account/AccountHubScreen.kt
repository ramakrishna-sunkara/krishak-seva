package com.kisanalert.presentation.account

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.core.ui.components.KisanAppIcon
import com.kisanalert.core.ui.KisanScaffoldDefaults
import com.kisanalert.core.ui.components.KisanTopAppBar
import com.kisanalert.core.ui.components.LanguagePreferenceSection
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.presentation.common.LanguagePreferenceEvent
import com.kisanalert.presentation.common.LanguagePreferenceViewModel
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.presentation.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountHubScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVoiceAssistant: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    languageViewModel: LanguagePreferenceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languageState by languageViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    LaunchedEffect(languageState.shouldRecreateActivity) {
        if (languageState.shouldRecreateActivity) {
            languageViewModel.onEvent(LanguagePreferenceEvent.RecreateHandled)
            activity.recreate()
        }
    }
    Scaffold(
        contentWindowInsets = KisanScaffoldDefaults.NestedTabContentInsets,
        topBar = {
            KisanTopAppBar(title = stringResource(R.string.nav_account))
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.dashboardData == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {
                AccountHubContent(
                    farmerName = uiState.dashboardData?.farmerName ?: "",
                    village = uiState.dashboardData?.village ?: "",
                    currentCrop = uiState.dashboardData?.currentCrop ?: "",
                    unreadNotificationCount = uiState.unreadNotificationCount,
                    selectedLanguage = languageState.selectedLanguage,
                    languageOptions = languageViewModel.languageOptions,
                    isLanguageUpdating = languageState.isUpdating,
                    onLanguageSelected = { language ->
                        languageViewModel.onEvent(LanguagePreferenceEvent.LanguageSelected(language))
                    },
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToVoiceAssistant = onNavigateToVoiceAssistant
                )
            }
        }
    }
}

@Composable
private fun AccountHubContent(
    farmerName: String,
    village: String,
    currentCrop: String,
    unreadNotificationCount: Int,
    selectedLanguage: PreferredLanguage,
    languageOptions: List<PreferredLanguage>,
    isLanguageUpdating: Boolean,
    onLanguageSelected: (PreferredLanguage) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVoiceAssistant: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AccountProfileCard(
                farmerName = farmerName,
                village = village,
                currentCrop = currentCrop
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LanguagePreferenceSection(
                    selectedLanguage = selectedLanguage,
                    languageOptions = languageOptions,
                    onLanguageSelected = onLanguageSelected,
                    isEnabled = !isLanguageUpdating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = KisanColors.CardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.account_menu_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        item {
            AccountMenuItem(
                icon = Icons.Rounded.Person,
                title = stringResource(R.string.account_menu_profile),
                subtitle = stringResource(R.string.account_menu_profile_hint),
                onClick = onNavigateToProfile
            )
        }
        item {
            AccountMenuItem(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.account_menu_notifications),
                subtitle = stringResource(R.string.account_menu_notifications_hint),
                badgeCount = unreadNotificationCount,
                onClick = onNavigateToNotifications
            )
        }
        item {
            AccountMenuItem(
                icon = Icons.Rounded.Mic,
                title = stringResource(R.string.account_menu_voice),
                subtitle = stringResource(R.string.account_menu_voice_hint),
                onClick = onNavigateToVoiceAssistant
            )
        }
        item {
            AccountMenuItem(
                icon = Icons.Rounded.Settings,
                title = stringResource(R.string.account_menu_settings),
                subtitle = stringResource(R.string.account_menu_settings_hint),
                onClick = onNavigateToSettings
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AccountProfileCard(
    farmerName: String,
    village: String,
    currentCrop: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = KisanColors.CardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KisanColors.AccentTint,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    KisanAppIcon(
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = farmerName.ifBlank { stringResource(R.string.account_guest_name) },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (village.isNotBlank()) {
                        Text(
                            text = village,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (currentCrop.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.dashboard_current_crop, currentCrop),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = KisanColors.CardBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KisanColors.AccentTint),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(text = badgeCount.toString())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
