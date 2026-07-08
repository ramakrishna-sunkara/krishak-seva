package com.kisanalert.presentation.profile

import com.kisanalert.domain.model.FarmerProfileValidationError
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource

enum class RegistrationStep {
    Personal,
    Farm,
    Crop
}

data class FarmerRegistrationUiState(
    val currentStep: RegistrationStep = RegistrationStep.Personal,
    val name: String = "",
    val pincode: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val postOfficeOptions: List<PostOfficeLocation> = emptyList(),
    val pincodeDistrictOptions: List<String> = emptyList(),
    val isPincodeLoading: Boolean = false,
    val pincodeLookupMessage: String? = null,
    val preferredLanguage: PreferredLanguage = PreferredLanguage.TELUGU,
    val farmSizeAcres: String = "",
    val soilType: SoilType? = null,
    val waterSource: WaterSource? = null,
    val currentCrop: String = "",
    val customCrop: String = "",
    val isLoading: Boolean = false,
    val validationError: FarmerProfileValidationError? = null,
    val errorMessage: String? = null,
    val shouldRecreateActivity: Boolean = false,
    val isCompleted: Boolean = false
)
