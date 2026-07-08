package com.kisanalert.presentation.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import com.kisanalert.core.ui.components.KisanAppIcon
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.app.Activity
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kisanalert.R
import com.kisanalert.core.ui.components.KisanDropdownField
import com.kisanalert.core.ui.components.KisanPrimaryButton
import com.kisanalert.core.ui.components.KisanTextField
import com.kisanalert.core.ui.components.LocationFormFields
import com.kisanalert.core.ui.theme.KrishakSevaTheme
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.core.ui.localization.messageResId
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerRegistrationScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: FarmerRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onNavigateToDashboard()
        }
    }
    LaunchedEffect(uiState.shouldRecreateActivity) {
        if (uiState.shouldRecreateActivity) {
            viewModel.onRecreateHandled()
            activity.recreate()
        }
    }
    LaunchedEffect(uiState.validationError) {
        val validationError = uiState.validationError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = context.getString(validationError.messageResId())
        )
        viewModel.onDismissError()
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onDismissError()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.registration_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (uiState.currentStep != RegistrationStep.Personal) {
                        IconButton(onClick = viewModel::onBackStep) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.auth_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (uiState.currentStep == RegistrationStep.Crop) {
                    KisanPrimaryButton(
                        text = stringResource(R.string.registration_complete),
                        onClick = viewModel::onSubmitProfile,
                        isLoading = uiState.isLoading
                    )
                } else {
                    KisanPrimaryButton(
                        text = stringResource(R.string.registration_next),
                        onClick = viewModel::onNextStep,
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    ) { innerPadding ->
        FarmerRegistrationContent(
            uiState = uiState,
            stateOptions = viewModel.stateOptions,
            cropOptions = viewModel.cropOptions,
            soilOptions = viewModel.soilOptions,
            waterSourceOptions = viewModel.waterSourceOptions,
            languageOptions = viewModel.languageOptions,
            modifier = Modifier.padding(innerPadding),
            onNameChanged = viewModel::onNameChanged,
            onPincodeChanged = viewModel::onPincodeChanged,
            onPostOfficeSelected = viewModel::onPostOfficeSelected,
            onVillageChanged = viewModel::onVillageChanged,
            onDistrictChanged = viewModel::onDistrictChanged,
            onStateChanged = viewModel::onStateChanged,
            onPreferredLanguageChanged = viewModel::onPreferredLanguageChanged,
            onFarmSizeChanged = viewModel::onFarmSizeChanged,
            onSoilTypeChanged = viewModel::onSoilTypeChanged,
            onWaterSourceChanged = viewModel::onWaterSourceChanged,
            onCurrentCropChanged = viewModel::onCurrentCropChanged,
            onCustomCropChanged = viewModel::onCustomCropChanged,
            onNextStep = viewModel::onNextStep,
            onSubmitProfile = viewModel::onSubmitProfile
        )
    }
}

@Composable
private fun FarmerRegistrationContent(
    uiState: FarmerRegistrationUiState,
    stateOptions: List<String>,
    cropOptions: List<String>,
    soilOptions: List<SoilType>,
    waterSourceOptions: List<WaterSource>,
    languageOptions: List<PreferredLanguage>,
    modifier: Modifier = Modifier,
    onNameChanged: (String) -> Unit,
    onPincodeChanged: (String) -> Unit,
    onPostOfficeSelected: (PostOfficeLocation) -> Unit,
    onVillageChanged: (String) -> Unit,
    onDistrictChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onPreferredLanguageChanged: (PreferredLanguage) -> Unit,
    onFarmSizeChanged: (String) -> Unit,
    onSoilTypeChanged: (SoilType) -> Unit,
    onWaterSourceChanged: (WaterSource) -> Unit,
    onCurrentCropChanged: (String) -> Unit,
    onCustomCropChanged: (String) -> Unit,
    onNextStep: () -> Unit,
    onSubmitProfile: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RegistrationProgressHeader(currentStep = uiState.currentStep)
        if (uiState.currentStep == RegistrationStep.Personal) {
            RegistrationOnboardingTipCard()
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "registration_step"
            ) { step ->
                when (step) {
                    RegistrationStep.Personal -> PersonalDetailsStep(
                        uiState = uiState,
                        stateOptions = stateOptions,
                        languageOptions = languageOptions,
                        onNameChanged = onNameChanged,
                        onPincodeChanged = onPincodeChanged,
                        onPostOfficeSelected = onPostOfficeSelected,
                        onVillageChanged = onVillageChanged,
                        onDistrictChanged = onDistrictChanged,
                        onStateChanged = onStateChanged,
                        onPreferredLanguageChanged = onPreferredLanguageChanged
                    )
                    RegistrationStep.Farm -> FarmDetailsStep(
                        uiState = uiState,
                        soilOptions = soilOptions,
                        waterSourceOptions = waterSourceOptions,
                        onFarmSizeChanged = onFarmSizeChanged,
                        onSoilTypeChanged = onSoilTypeChanged,
                        onWaterSourceChanged = onWaterSourceChanged
                    )
                    RegistrationStep.Crop -> CropDetailsStep(
                        uiState = uiState,
                        cropOptions = cropOptions,
                        onCurrentCropChanged = onCurrentCropChanged,
                        onCustomCropChanged = onCustomCropChanged
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RegistrationOnboardingTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.registration_onboarding_tip_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.registration_onboarding_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RegistrationProgressHeader(currentStep: RegistrationStep) {
    val stepIndex = when (currentStep) {
        RegistrationStep.Personal -> 1
        RegistrationStep.Farm -> 2
        RegistrationStep.Crop -> 3
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.registration_step_counter, stepIndex),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        LinearProgressIndicator(
            progress = { stepIndex / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepChip(
                icon = Icons.Rounded.Person,
                label = stringResource(R.string.registration_step_personal),
                isActive = currentStep == RegistrationStep.Personal
            )
            StepChip(
                icon = Icons.Rounded.WaterDrop,
                label = stringResource(R.string.registration_step_farm),
                isActive = currentStep == RegistrationStep.Farm
            )
            StepChip(
                icon = Icons.Rounded.Eco,
                label = stringResource(R.string.registration_step_crop),
                isActive = currentStep == RegistrationStep.Crop
            )
        }
    }
}

@Composable
private fun StepChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun PersonalDetailsStep(
    uiState: FarmerRegistrationUiState,
    stateOptions: List<String>,
    languageOptions: List<PreferredLanguage>,
    onNameChanged: (String) -> Unit,
    onPincodeChanged: (String) -> Unit,
    onPostOfficeSelected: (PostOfficeLocation) -> Unit,
    onVillageChanged: (String) -> Unit,
    onDistrictChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit,
    onPreferredLanguageChanged: (PreferredLanguage) -> Unit
) {
    StepContainer(
        title = stringResource(R.string.registration_personal_title),
        subtitle = stringResource(R.string.registration_personal_subtitle)
    ) {
        KisanTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = stringResource(R.string.registration_name_label),
            placeholder = stringResource(R.string.registration_name_placeholder),
            imeAction = ImeAction.Next
        )
        LocationFormFields(
            pincode = uiState.pincode,
            village = uiState.village,
            district = uiState.district,
            state = uiState.state,
            stateOptions = stateOptions,
            postOfficeOptions = uiState.postOfficeOptions,
            pincodeDistrictOptions = uiState.pincodeDistrictOptions,
            isPincodeLoading = uiState.isPincodeLoading,
            pincodeLookupMessage = uiState.pincodeLookupMessage,
            onPincodeChanged = onPincodeChanged,
            onPostOfficeSelected = onPostOfficeSelected,
            onVillageChanged = onVillageChanged,
            onDistrictChanged = onDistrictChanged,
            onStateChanged = onStateChanged
        )
        KisanDropdownField(
            label = stringResource(R.string.registration_language_label),
            options = languageOptions,
            selectedOption = uiState.preferredLanguage,
            onOptionSelected = onPreferredLanguageChanged,
            optionLabel = { language -> language.localizedLabel() }
        )
    }
}

@Composable
private fun FarmDetailsStep(
    uiState: FarmerRegistrationUiState,
    soilOptions: List<SoilType>,
    waterSourceOptions: List<WaterSource>,
    onFarmSizeChanged: (String) -> Unit,
    onSoilTypeChanged: (SoilType) -> Unit,
    onWaterSourceChanged: (WaterSource) -> Unit
) {
    StepContainer(
        title = stringResource(R.string.registration_farm_title),
        subtitle = stringResource(R.string.registration_farm_subtitle)
    ) {
        KisanTextField(
            value = uiState.farmSizeAcres,
            onValueChange = onFarmSizeChanged,
            label = stringResource(R.string.registration_farm_size_label),
            placeholder = stringResource(R.string.registration_farm_size_placeholder),
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        )
        KisanDropdownField(
            label = stringResource(R.string.registration_soil_label),
            options = soilOptions,
            selectedOption = uiState.soilType,
            onOptionSelected = onSoilTypeChanged,
            optionLabel = { soilType -> soilType.localizedLabel() }
        )
        KisanDropdownField(
            label = stringResource(R.string.registration_water_label),
            options = waterSourceOptions,
            selectedOption = uiState.waterSource,
            onOptionSelected = onWaterSourceChanged,
            optionLabel = { waterSource -> waterSource.localizedLabel() }
        )
    }
}

@Composable
private fun CropDetailsStep(
    uiState: FarmerRegistrationUiState,
    cropOptions: List<String>,
    onCurrentCropChanged: (String) -> Unit,
    onCustomCropChanged: (String) -> Unit
) {
    StepContainer(
        title = stringResource(R.string.registration_crop_title),
        subtitle = stringResource(R.string.registration_crop_subtitle)
    ) {
        KisanDropdownField(
            label = stringResource(R.string.registration_crop_label),
            options = cropOptions,
            selectedOption = uiState.currentCrop.takeIf { crop -> crop.isNotBlank() },
            onOptionSelected = onCurrentCropChanged,
            optionLabel = { crop -> crop }
        )
        if (uiState.currentCrop == "Other") {
            KisanTextField(
                value = uiState.customCrop,
                onValueChange = onCustomCropChanged,
                label = stringResource(R.string.registration_custom_crop_label),
                placeholder = stringResource(R.string.registration_custom_crop_placeholder),
                imeAction = ImeAction.Done
            )
        }
        RegistrationSummaryCard(uiState = uiState)
    }
}

@Composable
private fun RegistrationSummaryCard(uiState: FarmerRegistrationUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KisanAppIcon(
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.registration_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            SummaryRow(
                label = stringResource(R.string.registration_summary_name),
                value = uiState.name
            )
            SummaryRow(
                label = stringResource(R.string.registration_summary_location),
                value = "${uiState.village}, ${uiState.district}, ${uiState.state} • ${uiState.pincode}"
            )
            SummaryRow(
                label = stringResource(R.string.registration_summary_farm),
                value = "${uiState.farmSizeAcres} acres • ${uiState.soilType?.localizedLabel().orEmpty()}"
            )
            SummaryRow(
                label = stringResource(R.string.registration_summary_water),
                value = uiState.waterSource?.localizedLabel().orEmpty()
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StepContainer(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun FarmerRegistrationContentPreview() {
    KrishakSevaTheme {
        FarmerRegistrationContent(
            uiState = FarmerRegistrationUiState(
                name = "Rama Krishna",
                pincode = "521101",
                village = "Gannavaram",
                district = "Krishna",
                state = "Andhra Pradesh",
                farmSizeAcres = "2.5",
                soilType = SoilType.BLACK,
                waterSource = WaterSource.BOREWELL,
                currentCrop = "Cotton"
            ),
            stateOptions = listOf("Telangana", "Andhra Pradesh"),
            cropOptions = listOf("Cotton", "Rice"),
            soilOptions = SoilType.entries,
            waterSourceOptions = WaterSource.entries,
            languageOptions = PreferredLanguage.entries,
            onNameChanged = {},
            onPincodeChanged = {},
            onPostOfficeSelected = {},
            onVillageChanged = {},
            onDistrictChanged = {},
            onStateChanged = {},
            onPreferredLanguageChanged = {},
            onFarmSizeChanged = {},
            onSoilTypeChanged = {},
            onWaterSourceChanged = {},
            onCurrentCropChanged = {},
            onCustomCropChanged = {},
            onNextStep = {},
            onSubmitProfile = {}
        )
    }
}
