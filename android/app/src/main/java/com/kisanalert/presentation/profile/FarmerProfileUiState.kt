package com.kisanalert.presentation.profile

import com.kisanalert.domain.model.FarmerProfileValidationError
import com.kisanalert.domain.model.PostOfficeLocation
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource

enum class FarmerProfileMode {
    View,
    Edit
}

data class FarmerProfileUiState(
    val isLoading: Boolean = true,
    val mode: FarmerProfileMode = FarmerProfileMode.View,
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
    val isSynced: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val validationError: FarmerProfileValidationError? = null,
    val errorMessage: String? = null,
    val shouldRecreateActivity: Boolean = false
)
