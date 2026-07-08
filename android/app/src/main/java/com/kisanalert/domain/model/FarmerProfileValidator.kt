package com.kisanalert.domain.model

data class FarmerProfileValidationResult(
    val isValid: Boolean,
    val error: FarmerProfileValidationError? = null
)

object FarmerProfileValidator {
    fun validateName(name: String): FarmerProfileValidationResult {
        val trimmedName = name.trim()
        return if (trimmedName.length < 2) {
            FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.NAME_TOO_SHORT
            )
        } else {
            FarmerProfileValidationResult(isValid = true)
        }
    }

    fun validatePincode(pincode: String): FarmerProfileValidationResult {
        val trimmedPincode = pincode.trim()
        return if (trimmedPincode.length != 6 || trimmedPincode.any { character -> !character.isDigit() }) {
            FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.INVALID_PINCODE
            )
        } else {
            FarmerProfileValidationResult(isValid = true)
        }
    }

    fun validateLocation(
        pincode: String,
        village: String,
        district: String,
        state: String
    ): FarmerProfileValidationResult {
        val pincodeValidation = validatePincode(pincode)
        if (!pincodeValidation.isValid) {
            return pincodeValidation
        }
        return when {
            village.trim().isEmpty() -> FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.VILLAGE_REQUIRED
            )
            district.trim().isEmpty() -> FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.DISTRICT_REQUIRED
            )
            state.trim().isEmpty() -> FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.STATE_REQUIRED
            )
            else -> FarmerProfileValidationResult(isValid = true)
        }
    }

    fun validateFarmSize(farmSizeText: String): FarmerProfileValidationResult {
        val farmSize = farmSizeText.trim().toDoubleOrNull()
        return if (farmSize == null || farmSize <= 0.0) {
            FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.INVALID_FARM_SIZE
            )
        } else {
            FarmerProfileValidationResult(isValid = true)
        }
    }

    fun validateCurrentCrop(currentCrop: String): FarmerProfileValidationResult {
        return if (currentCrop.trim().isEmpty()) {
            FarmerProfileValidationResult(
                isValid = false,
                error = FarmerProfileValidationError.CURRENT_CROP_REQUIRED
            )
        } else {
            FarmerProfileValidationResult(isValid = true)
        }
    }
}
