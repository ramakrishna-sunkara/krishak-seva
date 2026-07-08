package com.kisanalert.presentation.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.kisanalert.R
import com.kisanalert.core.ui.components.KisanAppIcon
import com.kisanalert.core.ui.components.KisanPrimaryButton
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.kisanalert.core.ui.components.KisanTopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.rounded.AutoAwesome
import com.kisanalert.core.ui.KisanScaffoldDefaults
import com.kisanalert.core.ui.theme.KisanAlertTheme
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.domain.model.AlertType
import com.kisanalert.domain.model.AiRecommendation
import com.kisanalert.domain.model.CropHealthStatus
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.core.ui.localization.localizedMessage
import com.kisanalert.core.ui.localization.localizedTitle
import com.kisanalert.domain.model.DashboardAlert
import com.kisanalert.domain.model.DashboardData
import com.kisanalert.domain.model.IrrigationAdvice
import com.kisanalert.domain.model.WeatherSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCropRecommendation: () -> Unit,
    onNavigateToWeatherAdvisory: () -> Unit,
    onNavigateToCropDoctor: () -> Unit,
    onNavigateToVoiceAssistant: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isTabRoot: Boolean = false,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onEvent(DashboardEvent.DismissError)
        }
    }
    Scaffold(
        contentWindowInsets = KisanScaffoldDefaults.NestedTabContentInsets,
        topBar = {
            KisanTopAppBar(
                title = stringResource(R.string.dashboard_title),
                actions = {
                    IconButton(onClick = { viewModel.onEvent(DashboardEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.dashboard_refresh)
                        )
                    }
                    BadgedBox(
                        badge = {
                            if (uiState.unreadNotificationCount > 0) {
                                Badge {
                                    Text(text = uiState.unreadNotificationCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = stringResource(R.string.dashboard_notifications)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.dashboardData != null -> {
                DashboardContent(
                    dashboardData = uiState.dashboardData!!,
                    isRefreshing = uiState.isRefreshing,
                    showDashboardGuide = uiState.showDashboardGuide,
                    modifier = Modifier.padding(innerPadding),
                    onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) },
                    onDismissDashboardGuide = { viewModel.onEvent(DashboardEvent.DismissDashboardGuide) },
                    onNavigateToCropRecommendation = onNavigateToCropRecommendation,
                    onNavigateToWeatherAdvisory = onNavigateToWeatherAdvisory,
                    onNavigateToCropDoctor = onNavigateToCropDoctor
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: stringResource(R.string.dashboard_load_error),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        KisanPrimaryButton(
                            text = stringResource(R.string.dashboard_retry),
                            onClick = { viewModel.onEvent(DashboardEvent.Retry) },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    dashboardData: DashboardData,
    isRefreshing: Boolean,
    showDashboardGuide: Boolean,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onDismissDashboardGuide: () -> Unit,
    onNavigateToCropRecommendation: () -> Unit,
    onNavigateToWeatherAdvisory: () -> Unit,
    onNavigateToCropDoctor: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showDashboardGuide) {
                item {
                    DashboardGuideCard(onDismiss = onDismissDashboardGuide)
                }
            }
            item {
                WelcomeCard(
                    farmerName = dashboardData.farmerName,
                    village = dashboardData.village,
                    currentCrop = dashboardData.currentCrop
                )
            }
            item {
                SectionHeader(title = stringResource(R.string.dashboard_section_overview))
            }
            item {
                dashboardData.weather?.let { weather ->
                    WeatherCard(
                        weather = weather,
                        isFromCache = dashboardData.isWeatherFromCache,
                        onClick = onNavigateToWeatherAdvisory
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreCard(
                        title = stringResource(R.string.dashboard_water_score),
                        value = "${dashboardData.waterSavingScore}%",
                        icon = Icons.Rounded.WaterDrop,
                        color = KisanColors.WaterSaving,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        title = stringResource(R.string.dashboard_crop_health),
                        value = dashboardData.cropHealthStatus.localizedLabel(),
                        icon = Icons.Rounded.Eco,
                        color = when (dashboardData.cropHealthStatus) {
                            CropHealthStatus.GOOD -> KisanColors.CropHealthGood
                            CropHealthStatus.WARNING -> KisanColors.CropHealthWarning
                            CropHealthStatus.CRITICAL -> KisanColors.CropHealthCritical
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                SectionHeader(title = stringResource(R.string.dashboard_section_insights))
            }
            item {
                IrrigationAdviceCard(advice = dashboardData.irrigationAdvice)
            }
            item {
                AiRecommendationCard(
                    recommendation = dashboardData.aiRecommendation,
                    onClick = onNavigateToCropRecommendation
                )
            }
            item {
                Text(
                    text = stringResource(R.string.dashboard_recent_alerts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(dashboardData.recentAlerts) { alert ->
                AlertCard(alert = alert)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun DashboardGuideCard(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = KisanColors.AccentTint
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.dashboard_guide_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = stringResource(R.string.dashboard_guide_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dashboard_guide_dismiss))
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    farmerName: String,
    village: String,
    currentCrop: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                KisanAppIcon(
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_welcome, farmerName),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = village,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
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

@Composable
private fun WeatherCard(
    weather: WeatherSummary,
    isFromCache: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_weather_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${weather.temperatureCelsius.toInt()}°C",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    WeatherStat(label = "Humidity", value = "${weather.humidityPercent}%")
                    WeatherStat(label = "Rain", value = "${weather.rainVolumeMm} mm")
                    WeatherStat(label = "Wind", value = "${weather.windSpeedKmh.toInt()} km/h")
                }
            }
            if (isFromCache) {
                Text(
                    text = stringResource(R.string.dashboard_weather_cached),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeatherStat(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun ScoreCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun IrrigationAdviceCard(advice: IrrigationAdvice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (advice.shouldIrrigateToday) {
                KisanColors.AccentTint
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_irrigation_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = advice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = advice.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AiRecommendationCard(
    recommendation: AiRecommendation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.dashboard_ai_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recommendation.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = recommendation.actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AlertCard(alert: DashboardAlert) {
    val alertColor = when (alert.type) {
        AlertType.WEATHER -> MaterialTheme.colorScheme.tertiary
        AlertType.DISEASE -> KisanColors.CropHealthCritical
        AlertType.IRRIGATION -> KisanColors.WaterSaving
    }
    val dateFormat = remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = KisanColors.CardBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(alertColor)
                    .align(Alignment.Top)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = alert.localizedMessage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(alert.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    KisanAlertTheme {
        DashboardContent(
            dashboardData = DashboardData(
                farmerName = "Rama Krishna",
                village = "Gannavaram",
                currentCrop = "Cotton",
                weather = WeatherSummary(
                    temperatureCelsius = 34.0,
                    humidityPercent = 72,
                    rainVolumeMm = 0.0,
                    windSpeedKmh = 14.0,
                    description = "Partly cloudy",
                    cityName = "Krishna",
                    iconCode = "02d"
                ),
                waterSavingScore = 78,
                cropHealthStatus = CropHealthStatus.GOOD,
                irrigationAdvice = IrrigationAdvice(
                    title = "Irrigate Early Morning",
                    message = "High temperature expected. Irrigate cotton early morning.",
                    shouldIrrigateToday = true
                ),
                aiRecommendation = AiRecommendation(
                    cropName = "Cotton",
                    title = "AI Insight for Cotton",
                    message = "Monitor for pests in the next week.",
                    actionLabel = "View Crop Advice"
                ),
                recentAlerts = listOf(
                    DashboardAlert(
                        id = "1",
                        title = "Welcome",
                        message = "Profile set up successfully.",
                        type = AlertType.IRRIGATION,
                        timestamp = System.currentTimeMillis()
                    )
                )
            ),
            isRefreshing = false,
            showDashboardGuide = true,
            onRefresh = {},
            onDismissDashboardGuide = {},
            onNavigateToCropRecommendation = {},
            onNavigateToWeatherAdvisory = {},
            onNavigateToCropDoctor = {}
        )
    }
}
