package com.kisanalert.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import com.kisanalert.core.ui.components.KisanAppIcon
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.kisanalert.core.ui.components.KisanTopAppBar
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.core.ui.localization.messageResId
import com.kisanalert.core.ui.components.KisanDropdownField
import com.kisanalert.core.ui.components.KisanOutlinedButton
import com.kisanalert.core.ui.components.KisanPrimaryButton
import com.kisanalert.core.ui.components.KisanTextField
import com.kisanalert.core.ui.components.LocationFormFields
import com.kisanalert.core.ui.theme.KisanAlertTheme
import com.kisanalert.core.ui.theme.KisanColors
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: FarmerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val saveSuccessMessage = stringResource(R.string.profile_save_success)
    LaunchedEffect(uiState.shouldRecreateActivity) {
        if (uiState.shouldRecreateActivity) {
            viewModel.onEvent(FarmerProfileEvent.RecreateHandled)
            activity.recreate()
        }
    }
    LaunchedEffect(uiState.validationError) {
        val validationError = uiState.validationError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = context.getString(validationError.messageResId())
        )
        viewModel.onEvent(FarmerProfileEvent.DismissError)
    }
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.onEvent(FarmerProfileEvent.DismissError)
        }
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(message = saveSuccessMessage)
            viewModel.onEvent(FarmerProfileEvent.SaveSuccessHandled)
        }
    }
    Scaffold(
        topBar = {
            KisanTopAppBar(
                title = stringResource(R.string.profile_title),
                showBackButton = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (!uiState.isLoading && uiState.mode == FarmerProfileMode.View) {
                        IconButton(onClick = { viewModel.onEvent(FarmerProfileEvent.EditClicked) }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.profile_edit)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!uiState.isLoading && uiState.mode == FarmerProfileMode.Edit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KisanPrimaryButton(
                        text = stringResource(R.string.profile_save),
                        onClick = { viewModel.onEvent(FarmerProfileEvent.SaveClicked) },
                        isLoading = uiState.isSaving
                    )
                    KisanOutlinedButton(
                        text = stringResource(R.string.profile_cancel),
                        onClick = { viewModel.onEvent(FarmerProfileEvent.CancelEdit) },
                        enabled = !uiState.isSaving
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.mode == FarmerProfileMode.View -> {
                ProfileViewContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onEditClick = { viewModel.onEvent(FarmerProfileEvent.EditClicked) }
                )
            }
            else -> {
                ProfileEditContent(
                    uiState = uiState,
                    stateOptions = viewModel.stateOptions,
                    cropOptions = viewModel.cropOptions,
                    soilOptions = viewModel.soilOptions,
                    waterSourceOptions = viewModel.waterSourceOptions,
                    languageOptions = viewModel.languageOptions,
                    modifier = Modifier.padding(innerPadding),
                    onNameChanged = { viewModel.onEvent(FarmerProfileEvent.NameChanged(it)) },
                    onPincodeChanged = { viewModel.onEvent(FarmerProfileEvent.PincodeChanged(it)) },
                    onPostOfficeSelected = {
                        viewModel.onEvent(FarmerProfileEvent.PostOfficeSelected(it))
                    },
                    onVillageChanged = { viewModel.onEvent(FarmerProfileEvent.VillageChanged(it)) },
                    onDistrictChanged = { viewModel.onEvent(FarmerProfileEvent.DistrictChanged(it)) },
                    onStateChanged = { viewModel.onEvent(FarmerProfileEvent.StateChanged(it)) },
                    onPreferredLanguageChanged = {
                        viewModel.onEvent(FarmerProfileEvent.PreferredLanguageChanged(it))
                    },
                    onFarmSizeChanged = { viewModel.onEvent(FarmerProfileEvent.FarmSizeChanged(it)) },
                    onSoilTypeChanged = { viewModel.onEvent(FarmerProfileEvent.SoilTypeChanged(it)) },
                    onWaterSourceChanged = {
                        viewModel.onEvent(FarmerProfileEvent.WaterSourceChanged(it))
                    },
                    onCurrentCropChanged = { viewModel.onEvent(FarmerProfileEvent.CurrentCropChanged(it)) },
                    onCustomCropChanged = { viewModel.onEvent(FarmerProfileEvent.CustomCropChanged(it)) }
                )
            }
        }
    }
}

@Composable
private fun ProfileViewContent(
    uiState: FarmerProfileUiState,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit
) {
    val resolvedCrop = if (uiState.currentCrop == "Other") {
        uiState.customCrop
    } else {
        uiState.currentCrop
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeaderCard(
            name = uiState.name,
            village = uiState.village,
            district = uiState.district,
            state = uiState.state,
            language = uiState.preferredLanguage.localizedLabel(),
            isSynced = uiState.isSynced
        )
        ProfileInfoCard(
            title = stringResource(R.string.profile_section_personal),
            icon = Icons.Rounded.Person,
            rows = listOf(
                stringResource(R.string.registration_name_label) to uiState.name,
                stringResource(R.string.registration_language_label) to uiState.preferredLanguage.localizedLabel()
            )
        )
        ProfileInfoCard(
            title = stringResource(R.string.profile_section_location),
            icon = Icons.Rounded.LocationOn,
            rows = listOf(
                stringResource(R.string.registration_village_label) to uiState.village,
                stringResource(R.string.registration_district_label) to uiState.district,
                stringResource(R.string.registration_state_label) to uiState.state
            )
        )
        ProfileInfoCard(
            title = stringResource(R.string.profile_section_farm),
            icon = Icons.Rounded.WaterDrop,
            rows = listOf(
                stringResource(R.string.registration_farm_size_label) to "${uiState.farmSizeAcres} acres",
                stringResource(R.string.registration_soil_label) to uiState.soilType?.localizedLabel().orEmpty(),
                stringResource(R.string.registration_water_label) to uiState.waterSource?.localizedLabel().orEmpty()
            )
        )
        ProfileInfoCard(
            title = stringResource(R.string.profile_section_crop),
            icon = Icons.Rounded.Eco,
            rows = listOf(
                stringResource(R.string.registration_crop_label) to resolvedCrop
            )
        )
        KisanPrimaryButton(
            text = stringResource(R.string.profile_edit),
            onClick = onEditClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeaderCard(
    name: String,
    village: String,
    district: String,
    state: String,
    language: String,
    isSynced: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            KisanColors.CardGradientStart,
                            KisanColors.CardGradientEnd
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        KisanAppIcon(
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    SyncStatusChip(isSynced = isSynced)
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "$village, $district",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeaderChip(icon = Icons.Rounded.LocationOn, label = state)
                    HeaderChip(icon = Icons.Rounded.Language, label = language)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusChip(isSynced: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isSynced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(
                    if (isSynced) R.string.profile_synced else R.string.profile_pending_sync
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    title: String,
    icon: ImageVector,
    rows: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            rows.forEach { (label, value) ->
                ProfileInfoRow(label = label, value = value)
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileEditContent(
    uiState: FarmerProfileUiState,
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
    onCustomCropChanged: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileEditSection(
                    title = stringResource(R.string.profile_section_personal),
                    subtitle = stringResource(R.string.registration_personal_subtitle)
                ) {
                    KisanTextField(
                        value = uiState.name,
                        onValueChange = onNameChanged,
                        label = stringResource(R.string.registration_name_label),
                        placeholder = stringResource(R.string.registration_name_placeholder),
                        imeAction = ImeAction.Next
                    )
                    KisanDropdownField(
                        label = stringResource(R.string.registration_language_label),
                        options = languageOptions,
                        selectedOption = uiState.preferredLanguage,
                        onOptionSelected = onPreferredLanguageChanged,
                        optionLabel = { language -> language.localizedLabel() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileEditSection(
                    title = stringResource(R.string.profile_section_location),
                    subtitle = stringResource(R.string.registration_location_subtitle)
                ) {
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
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileEditSection(
                    title = stringResource(R.string.profile_section_farm),
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
                Spacer(modifier = Modifier.height(8.dp))
                ProfileEditSection(
                    title = stringResource(R.string.profile_section_crop),
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
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileEditSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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

@Preview(showBackground = true)
@Composable
private fun ProfileViewContentPreview() {
    KisanAlertTheme {
        ProfileViewContent(
            uiState = FarmerProfileUiState(
                isLoading = false,
                name = "Rama Krishna",
                village = "Gannavaram",
                district = "Krishna",
                state = "Andhra Pradesh",
                farmSizeAcres = "2.5",
                soilType = SoilType.BLACK,
                waterSource = WaterSource.BOREWELL,
                currentCrop = "Cotton",
                isSynced = true
            ),
            onEditClick = {}
        )
    }
}
