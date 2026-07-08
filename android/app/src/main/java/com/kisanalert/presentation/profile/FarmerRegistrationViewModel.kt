package com.kisanalert.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanalert.core.constants.FarmOptions
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.FarmerProfileValidationError
import com.kisanalert.domain.model.FarmerProfileValidationResult
import com.kisanalert.domain.model.FarmerProfileValidator
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource
import com.kisanalert.domain.usecase.GetCurrentUserIdUseCase
import com.kisanalert.domain.usecase.LookupPincodeUseCase
import com.kisanalert.domain.usecase.SaveFarmerProfileUseCase
import com.kisanalert.domain.usecase.SetAppLocaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerRegistrationViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val saveFarmerProfileUseCase: SaveFarmerProfileUseCase,
    private val setAppLocaleUseCase: SetAppLocaleUseCase,
    private val lookupPincodeUseCase: LookupPincodeUseCase
) : ViewModel() {
    private val _uiState: MutableStateFlow<FarmerRegistrationUiState> =
        MutableStateFlow(FarmerRegistrationUiState())
    val uiState: StateFlow<FarmerRegistrationUiState> = _uiState.asStateFlow()

    val stateOptions: List<String> = FarmOptions.INDIAN_STATES
    val cropOptions: List<String> = FarmOptions.COMMON_CROPS
    val soilOptions: List<SoilType> = SoilType.entries
    val waterSourceOptions: List<WaterSource> = WaterSource.entries
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    fun onNameChanged(name: String) {
        _uiState.update { currentState ->
            currentState.copy(name = name, validationError = null, errorMessage = null)
        }
    }

    fun onPincodeChanged(pincodeInput: String) {
        val pincode = pincodeInput.filter { character -> character.isDigit() }.take(6)
        _uiState.update { currentState ->
            currentState.copy(
                pincode = pincode,
                pincodeLookupMessage = null,
                validationError = null,
                errorMessage = null
            )
        }
        if (pincode.length < 6) {
            _uiState.update { currentState ->
                currentState.copy(
                    postOfficeOptions = emptyList(),
                    pincodeDistrictOptions = emptyList(),
                    village = "",
                    district = "",
                    state = ""
                )
            }
            return
        }
        lookupPincode(pincode)
    }

    fun onPostOfficeSelected(postOffice: PostOfficeLocation) {
        _uiState.update { currentState ->
            currentState.copy(
                village = postOffice.name,
                district = postOffice.district,
                state = postOffice.state,
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun onVillageChanged(village: String) {
        _uiState.update { currentState ->
            currentState.copy(village = village, validationError = null, errorMessage = null)
        }
    }

    fun onDistrictChanged(district: String) {
        _uiState.update { currentState ->
            currentState.copy(district = district, validationError = null, errorMessage = null)
        }
    }

    fun onStateChanged(state: String) {
        _uiState.update { currentState ->
            val manualDistricts = FarmOptions.STATE_DISTRICTS[state].orEmpty()
            val district = if (currentState.district in manualDistricts) {
                currentState.district
            } else {
                ""
            }
            currentState.copy(
                state = state,
                district = district,
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun onPreferredLanguageChanged(language: PreferredLanguage) {
        if (language == _uiState.value.preferredLanguage) {
            return
        }
        _uiState.update { currentState ->
            currentState.copy(preferredLanguage = language, validationError = null, errorMessage = null)
        }
        viewModelScope.launch {
            setAppLocaleUseCase.execute(language)
            _uiState.update { currentState ->
                currentState.copy(shouldRecreateActivity = true)
            }
        }
    }

    fun onRecreateHandled() {
        _uiState.update { currentState ->
            currentState.copy(shouldRecreateActivity = false)
        }
    }

    fun onFarmSizeChanged(farmSize: String) {
        val sanitizedValue = farmSize.filter { character ->
            character.isDigit() || character == '.'
        }
        _uiState.update { currentState ->
            currentState.copy(farmSizeAcres = sanitizedValue, validationError = null, errorMessage = null)
        }
    }

    fun onSoilTypeChanged(soilType: SoilType) {
        _uiState.update { currentState ->
            currentState.copy(soilType = soilType, validationError = null, errorMessage = null)
        }
    }

    fun onWaterSourceChanged(waterSource: WaterSource) {
        _uiState.update { currentState ->
            currentState.copy(waterSource = waterSource, validationError = null, errorMessage = null)
        }
    }

    fun onCurrentCropChanged(crop: String) {
        _uiState.update { currentState ->
            currentState.copy(
                currentCrop = crop,
                customCrop = if (crop == "Other") currentState.customCrop else "",
                validationError = null,
                errorMessage = null
            )
        }
    }

    fun onCustomCropChanged(customCrop: String) {
        _uiState.update { currentState ->
            currentState.copy(customCrop = customCrop, validationError = null, errorMessage = null)
        }
    }

    fun onBackStep() {
        _uiState.update { currentState ->
            val previousStep = when (currentState.currentStep) {
                RegistrationStep.Personal -> RegistrationStep.Personal
                RegistrationStep.Farm -> RegistrationStep.Personal
                RegistrationStep.Crop -> RegistrationStep.Farm
            }
            currentState.copy(currentStep = previousStep, validationError = null, errorMessage = null)
        }
    }

    fun onNextStep() {
        val currentState = _uiState.value
        val validationResult = validateCurrentStep(currentState)
        if (!validationResult.isValid) {
            _uiState.update { state ->
                state.copy(validationError = validationResult.error)
            }
            return
        }
        _uiState.update { state ->
            val nextStep = when (state.currentStep) {
                RegistrationStep.Personal -> RegistrationStep.Farm
                RegistrationStep.Farm -> RegistrationStep.Crop
                RegistrationStep.Crop -> RegistrationStep.Crop
            }
            state.copy(currentStep = nextStep, validationError = null, errorMessage = null)
        }
    }

    fun onSubmitProfile() {
        val currentState = _uiState.value
        val cropValidation = FarmerProfileValidator.validateCurrentCrop(
            resolveCurrentCrop(currentState)
        )
        if (!cropValidation.isValid) {
            _uiState.update { state ->
                state.copy(validationError = cropValidation.error)
            }
            return
        }
        viewModelScope.launch {
            val userId = getCurrentUserIdUseCase.execute()
            if (userId == null) {
                _uiState.update { state ->
                    state.copy(errorMessage = "User session not found. Please sign in again.")
                }
                return@launch
            }
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, validationError = null)
            }
            val profile = buildFarmerProfile(currentState, userId)
            setAppLocaleUseCase.execute(profile.preferredLanguage)
            when (val result = saveFarmerProfileUseCase.execute(profile)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isCompleted = true,
                        errorMessage = null
                    )
                }
                is Result.Error -> _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onDismissError() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null, validationError = null)
        }
    }

    private fun validateCurrentStep(state: FarmerRegistrationUiState): FarmerProfileValidationResult {
        return when (state.currentStep) {
            RegistrationStep.Personal -> {
                val nameValidation = FarmerProfileValidator.validateName(state.name)
                if (!nameValidation.isValid) {
                    return nameValidation
                }
                FarmerProfileValidator.validateLocation(
                    pincode = state.pincode,
                    village = state.village,
                    district = state.district,
                    state = state.state
                )
            }
            RegistrationStep.Farm -> {
                val farmSizeValidation = FarmerProfileValidator.validateFarmSize(state.farmSizeAcres)
                if (!farmSizeValidation.isValid) {
                    return farmSizeValidation
                }
                if (state.soilType == null) {
                    return FarmerProfileValidationResult(
                        isValid = false,
                        error = FarmerProfileValidationError.SOIL_TYPE_REQUIRED
                    )
                }
                if (state.waterSource == null) {
                    return FarmerProfileValidationResult(
                        isValid = false,
                        error = FarmerProfileValidationError.WATER_SOURCE_REQUIRED
                    )
                }
                FarmerProfileValidationResult(isValid = true)
            }
            RegistrationStep.Crop -> {
                FarmerProfileValidator.validateCurrentCrop(resolveCurrentCrop(state))
            }
        }
    }

    private fun resolveCurrentCrop(state: FarmerRegistrationUiState): String {
        return if (state.currentCrop == "Other") {
            state.customCrop.trim()
        } else {
            state.currentCrop.trim()
        }
    }

    private fun buildFarmerProfile(
        state: FarmerRegistrationUiState,
        userId: String
    ): FarmerProfile {
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

    private fun lookupPincode(pincode: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(isPincodeLoading = true, pincodeLookupMessage = null)
            }
            when (val result = lookupPincodeUseCase.execute(pincode)) {
                is Result.Success -> {
                    val lookupResult = result.data
                    _uiState.update { currentState ->
                        currentState.copy(
                            isPincodeLoading = false,
                            postOfficeOptions = lookupResult.postOffices,
                            pincodeDistrictOptions = lookupResult.districts,
                            state = lookupResult.postOffices.firstOrNull()?.state.orEmpty(),
                            district = "",
                            village = "",
                            pincodeLookupMessage = null
                        )
                    }
                }
                is Result.Error -> _uiState.update { currentState ->
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
