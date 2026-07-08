package com.kisanalert.presentation.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.kisanalert.core.ui.KisanScaffoldDefaults
import com.kisanalert.core.ui.components.KisanTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.core.ui.components.DataSourceBadge
import com.kisanalert.core.ui.components.KisanPrimaryButton
import com.kisanalert.core.ui.components.ServerErrorCard
import com.kisanalert.core.ui.theme.KisanAlertTheme
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.domain.model.CropRecommendation
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.domain.model.FarmingSeason

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CropRecommendationScreen(
    onNavigateBack: () -> Unit,
    isTabRoot: Boolean = false,
    viewModel: CropRecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        contentWindowInsets = KisanScaffoldDefaults.NestedTabContentInsets,
        topBar = {
            KisanTopAppBar(
                title = stringResource(R.string.crop_recommendation_title),
                showBackButton = !isTabRoot,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.onEvent(CropRecommendationEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.dashboard_refresh)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        CropRecommendationContent(
            uiState = uiState,
            seasonOptions = viewModel.seasonOptions,
            modifier = Modifier.padding(innerPadding),
            onSeasonSelected = { season ->
                viewModel.onEvent(CropRecommendationEvent.SeasonSelected(season))
            },
            onRefresh = { viewModel.onEvent(CropRecommendationEvent.Refresh) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CropRecommendationContent(
    uiState: CropRecommendationUiState,
    seasonOptions: List<FarmingSeason>,
    modifier: Modifier = Modifier,
    onSeasonSelected: (FarmingSeason) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CropRecommendationHeader(
                farmerLocation = uiState.farmerLocation,
                currentCrop = uiState.currentCrop,
                isOfflineMode = uiState.isOfflineMode
            )
        }
        item {
            Text(
                text = stringResource(R.string.crop_recommendation_season_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                seasonOptions.forEach { season ->
                    FilterChip(
                        selected = uiState.selectedSeason == season,
                        onClick = { onSeasonSelected(season) },
                        label = { Text(text = season.localizedLabel()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
        if (uiState.errorMessage != null) {
            item {
                ServerErrorCard(
                    message = uiState.errorMessage!!,
                    onRetry = onRefresh,
                    isRetrying = uiState.isLoading
                )
            }
        } else if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.crop_recommendation_loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else if (uiState.recommendations.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.crop_recommendation_empty),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    KisanPrimaryButton(
                        text = stringResource(R.string.crop_recommendation_generate),
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        } else {
            items(uiState.recommendations) { recommendation ->
                CropRecommendationCard(recommendation = recommendation)
            }
        }
    }
}

@Composable
private fun CropRecommendationHeader(
    farmerLocation: String,
    currentCrop: String,
    isOfflineMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            KisanColors.CardGradientStart,
                            KisanColors.CardGradientEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = stringResource(R.string.crop_recommendation_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (farmerLocation.isNotBlank()) {
                    Text(
                        text = farmerLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                if (currentCrop.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.dashboard_current_crop, currentCrop),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                DataSourceBadge(isFromCloud = !isOfflineMode)
            }
        }
    }
}

@Composable
private fun CropRecommendationCard(recommendation: CropRecommendation) {
    val riskColor = when {
        recommendation.riskScore <= 30 -> KisanColors.CropHealthGood
        recommendation.riskScore <= 60 -> KisanColors.CropHealthWarning
        else -> KisanColors.CropHealthCritical
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = recommendation.cropName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.crop_recommendation_risk, recommendation.riskScore),
                            style = MaterialTheme.typography.labelMedium,
                            color = riskColor
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { recommendation.riskScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = riskColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            RecommendationDetailRow(
                icon = Icons.Rounded.Science,
                label = stringResource(R.string.crop_recommendation_reason),
                value = recommendation.reason
            )
            RecommendationDetailRow(
                icon = Icons.Rounded.WaterDrop,
                label = stringResource(R.string.crop_recommendation_water),
                value = recommendation.waterRequirement
            )
            RecommendationDetailRow(
                icon = Icons.Rounded.Eco,
                label = stringResource(R.string.crop_recommendation_yield),
                value = recommendation.expectedYield
            )
            RecommendationDetailRow(
                icon = Icons.Rounded.Science,
                label = stringResource(R.string.crop_recommendation_fertilizer),
                value = recommendation.fertilizerAdvice
            )
        }
    }
}

@Composable
private fun RecommendationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CropRecommendationCardPreview() {
    KisanAlertTheme {
        CropRecommendationCard(
            recommendation = CropRecommendation(
                cropName = "Cotton",
                reason = "Ideal for black soil in monsoon season.",
                riskScore = 35,
                waterRequirement = "Medium",
                expectedYield = "8-12 quintals/acre",
                fertilizerAdvice = "Apply NPK in split doses with zinc supplement."
            )
        )
    }
}
