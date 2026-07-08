package com.kisanalert.presentation.profile

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.constants.FarmOptions
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.FarmerProfileValidator
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource
import com.kisanalert.domain.model.FarmerProfileValidationError
import com.kisanalert.domain.usecase.GetCurrentUserIdUseCase
import com.kisanalert.domain.usecase.LookupPincodeUseCase
import com.kisanalert.domain.usecase.ObserveFarmerProfileUseCase
import com.kisanalert.domain.usecase.SaveFarmerProfileUseCase
import com.kisanalert.domain.usecase.UpdatePreferredLanguageUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FarmerProfileEvent {
    data object ScreenOpened : FarmerProfileEvent
    data object EditClicked : FarmerProfileEvent
    data object CancelEdit : FarmerProfileEvent
    data object SaveClicked : FarmerProfileEvent
    data object SaveSuccessHandled : FarmerProfileEvent
    data object RecreateHandled : FarmerProfileEvent
    data object DismissError : FarmerProfileEvent
    data class NameChanged(val name: String) : FarmerProfileEvent
    data class PincodeChanged(val pincode: String) : FarmerProfileEvent
    data class PostOfficeSelected(val postOffice: PostOfficeLocation) : FarmerProfileEvent
    data class VillageChanged(val village: String) : FarmerProfileEvent
    data class DistrictChanged(val district: String) : FarmerProfileEvent
    data class StateChanged(val state: String) : FarmerProfileEvent
    data class PreferredLanguageChanged(val language: PreferredLanguage) : FarmerProfileEvent
    data class FarmSizeChanged(val farmSize: String) : FarmerProfileEvent
    data class SoilTypeChanged(val soilType: SoilType) : FarmerProfileEvent
    data class WaterSourceChanged(val waterSource: WaterSource) : FarmerProfileEvent
    data class CurrentCropChanged(val crop: String) : FarmerProfileEvent
    data class CustomCropChanged(val customCrop: String) : FarmerProfileEvent
}

@HiltViewModel
class FarmerProfileViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeFarmerProfileUseCase: ObserveFarmerProfileUseCase,
    private val saveFarmerProfileUseCase: SaveFarmerProfileUseCase,
    private val lookupPincodeUseCase: LookupPincodeUseCase,
    private val updatePreferredLanguageUseCase: UpdatePreferredLanguageUseCase
) : MviViewModel<FarmerProfileEvent, FarmerProfileUiState>(
    initialState = FarmerProfileUiState()
) {
    private var observeJob: Job? = null
    private var loadedProfile: FarmerProfile? = null

    val stateOptions: List<String> = FarmOptions.INDIAN_STATES
    val cropOptions: List<String> = FarmOptions.COMMON_CROPS
    val soilOptions: List<SoilType> = SoilType.entries
    val waterSourceOptions: List<WaterSource> = WaterSource.entries
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    init {
        onEvent(FarmerProfileEvent.ScreenOpened)
    }

    override fun onEvent(event: FarmerProfileEvent) {
        when (event) {
            FarmerProfileEvent.ScreenOpened -> observeProfile()
            FarmerProfileEvent.EditClicked -> {
                setState { currentState ->
                    currentState.copy(mode = FarmerProfileMode.Edit, errorMessage = null)
                }
            }
            FarmerProfileEvent.CancelEdit -> restoreFromLoadedProfile()
            FarmerProfileEvent.SaveClicked -> saveProfile()
            FarmerProfileEvent.SaveSuccessHandled -> {
                setState { currentState -> currentState.copy(saveSuccess = false) }
            }
            FarmerProfileEvent.RecreateHandled -> {
                setState { currentState -> currentState.copy(shouldRecreateActivity = false) }
            }
            FarmerProfileEvent.DismissError -> {
                setState { currentState ->
                    currentState.copy(errorMessage = null, validationError = null)
                }
            }
            is FarmerProfileEvent.NameChanged -> updateFormField { state ->
                state.copy(name = event.name, validationError = null, errorMessage = null)
            }
            is FarmerProfileEvent.PincodeChanged -> handlePincodeChanged(event.pincode)
            is FarmerProfileEvent.PostOfficeSelected -> updateFormField { state ->
                state.copy(
                    village = event.postOffice.name,
                    district = event.postOffice.district,
                    state = event.postOffice.state,
                    errorMessage = null
                )
            }
            is FarmerProfileEvent.VillageChanged -> updateFormField { state ->
                state.copy(village = event.village, errorMessage = null)
            }
            is FarmerProfileEvent.DistrictChanged -> updateFormField { state ->
                state.copy(district = event.district, errorMessage = null)
            }
            is FarmerProfileEvent.StateChanged -> updateFormField { state ->
                val manualDistricts = FarmOptions.STATE_DISTRICTS[event.state].orEmpty()
                val district = if (state.district in manualDistricts) state.district else ""
                state.copy(state = event.state, district = district, errorMessage = null)
            }
            is FarmerProfileEvent.PreferredLanguageChanged -> updateLanguage(event.language)
            is FarmerProfileEvent.FarmSizeChanged -> {
                val sanitizedValue = event.farmSize.filter { character ->
                    character.isDigit() || character == '.'
                }
                updateFormField { state ->
                    state.copy(farmSizeAcres = sanitizedValue, errorMessage = null)
                }
            }
            is FarmerProfileEvent.SoilTypeChanged -> updateFormField { state ->
                state.copy(soilType = event.soilType, errorMessage = null)
            }
            is FarmerProfileEvent.WaterSourceChanged -> updateFormField { state ->
                state.copy(waterSource = event.waterSource, errorMessage = null)
            }
            is FarmerProfileEvent.CurrentCropChanged -> updateFormField { state ->
                state.copy(
                    currentCrop = event.crop,
                    customCrop = if (event.crop == "Other") state.customCrop else "",
                    errorMessage = null
                )
            }
            is FarmerProfileEvent.CustomCropChanged -> updateFormField { state ->
                state.copy(customCrop = event.customCrop, errorMessage = null)
            }
        }
    }

    private fun updateFormField(reducer: (FarmerProfileUiState) -> FarmerProfileUiState) {
        setState { currentState -> reducer(currentState) }
    }

    private fun observeProfile() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val userId = getCurrentUserIdUseCase.execute()
            if (userId == null) {
                setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Please sign in to view your profile."
                    )
                }
                return@launch
            }
            observeFarmerProfileUseCase.execute(userId).collectLatest { profile ->
                if (profile == null) {
                    setState { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "Profile not found. Complete registration first."
                        )
                    }
                    return@collectLatest
                }
                loadedProfile = profile
                if (currentState.mode == FarmerProfileMode.View) {
                    setState { mapProfileToUiState(profile) }
                } else {
                    setState { currentState ->
                        currentState.copy(isSynced = profile.isSynced)
                    }
                }
            }
        }
    }

    private fun restoreFromLoadedProfile() {
        val profile = loadedProfile
        if (profile == null) {
            setState { currentState ->
                currentState.copy(mode = FarmerProfileMode.View, errorMessage = null)
            }
            return
        }
        setState { mapProfileToUiState(profile) }
    }

    private fun saveProfile() {
        val state = currentState
        val validationError = validateForm(state)
        if (validationError != null) {
            setState { currentState -> currentState.copy(validationError = validationError) }
            return
        }
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase.execute()
            if (userId == null) {
                setState { currentState ->
                    currentState.copy(errorMessage = "User session not found. Please sign in again.")
                }
                return@launch
            }
            setState { currentState ->
                currentState.copy(isSaving = true, errorMessage = null)
            }
            val profile = buildFarmerProfile(state, userId)
            when (val result = saveFarmerProfileUseCase.execute(profile)) {
                is Result.Success -> {
                    loadedProfile = result.data
                    setState { currentState ->
                        mapProfileToUiState(result.data).copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun validateForm(state: FarmerProfileUiState): FarmerProfileValidationError? {
        val nameValidation = FarmerProfileValidator.validateName(state.name)
        if (!nameValidation.isValid) {
            return nameValidation.error
        }
        val locationValidation = FarmerProfileValidator.validateLocation(
            pincode = state.pincode,
            village = state.village,
            district = state.district,
            state = state.state
        )
        if (!locationValidation.isValid) {
            return locationValidation.error
        }
        val farmSizeValidation = FarmerProfileValidator.validateFarmSize(state.farmSizeAcres)
        if (!farmSizeValidation.isValid) {
            return farmSizeValidation.error
        }
        if (state.soilType == null) {
            return FarmerProfileValidationError.SOIL_TYPE_REQUIRED
        }
        if (state.waterSource == null) {
            return FarmerProfileValidationError.WATER_SOURCE_REQUIRED
        }
        val cropValidation = FarmerProfileValidator.validateCurrentCrop(resolveCurrentCrop(state))
        if (!cropValidation.isValid) {
            return cropValidation.error
        }
        return null
    }

    private fun updateLanguage(language: PreferredLanguage) {
        if (language == currentState.preferredLanguage) {
            return
        }
        updateFormField { state ->
            state.copy(preferredLanguage = language, validationError = null, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = updatePreferredLanguageUseCase.execute(language)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(shouldRecreateActivity = true)
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(errorMessage = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun resolveCurrentCrop(state: FarmerProfileUiState): String {
        return if (state.currentCrop == "Other") {
            state.customCrop.trim()
        } else {
            state.currentCrop.trim()
        }
    }

    private fun buildFarmerProfile(state: FarmerProfileUiState, userId: String): FarmerProfile {
        return FarmerProfile(
            userId = userId,
            name = state.name.trim(),
            pincode = state.pincode.trim(),
            village = state.village.trim(),
            district = state.district.trim(),
            state = state.state,
            farmSizeAcres = state.farmSizeAcres.toDouble(),
            soilType = state.soilType ?: SoilType.LOAMY,
            waterSource = state.waterSource ?: WaterSource.RAIN_FED,
            preferredLanguage = state.preferredLanguage,
            currentCrop = resolveCurrentCrop(state)
        )
    }

    private fun mapProfileToUiState(profile: FarmerProfile): FarmerProfileUiState {
        val cropInList = profile.currentCrop in FarmOptions.COMMON_CROPS
        return FarmerProfileUiState(
            isLoading = false,
            mode = FarmerProfileMode.View,
            name = profile.name,
            pincode = profile.pincode,
            village = profile.village,
            district = profile.district,
            state = profile.state,
            preferredLanguage = profile.preferredLanguage,
            farmSizeAcres = profile.farmSizeAcres.toString(),
            soilType = profile.soilType,
            waterSource = profile.waterSource,
            currentCrop = if (cropInList) profile.currentCrop else "Other",
            customCrop = if (cropInList) "" else profile.currentCrop,
            isSynced = profile.isSynced,
            isSaving = false,
            saveSuccess = false,
            errorMessage = null
        )
    }

    private fun handlePincodeChanged(pincodeInput: String) {
        val pincode = pincodeInput.filter { character -> character.isDigit() }.take(6)
        updateFormField { state ->
            state.copy(
                pincode = pincode,
                pincodeLookupMessage = null,
                errorMessage = null
            )
        }
        if (pincode.length < 6) {
            updateFormField { state ->
                state.copy(
                    postOfficeOptions = emptyList(),
                    pincodeDistrictOptions = emptyList()
                )
            }
            return
        }
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isPincodeLoading = true, pincodeLookupMessage = null)
            }
            when (val result = lookupPincodeUseCase.execute(pincode)) {
                is Result.Success -> {
                    val lookupResult = result.data
                    setState { currentState ->
                        currentState.copy(
                            isPincodeLoading = false,
                            postOfficeOptions = lookupResult.postOffices,
                            pincodeDistrictOptions = lookupResult.districts,
                            state = lookupResult.postOffices.firstOrNull()?.state ?: currentState.state,
                            district = lookupResult.districts.singleOrNull() ?: currentState.district,
                            pincodeLookupMessage = null
                        )
                    }
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isPincodeLoading = false,
                        postOfficeOptions = emptyList(),
                        pincodeDistrictOptions = emptyList(),
                        pincodeLookupMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}
