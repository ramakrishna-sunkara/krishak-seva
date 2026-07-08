package com.kisanalert.domain.model

enum class FarmerProfileValidationError {
    NAME_TOO_SHORT,
    INVALID_PINCODE,
    VILLAGE_REQUIRED,
    DISTRICT_REQUIRED,
    STATE_REQUIRED,
    INVALID_FARM_SIZE,
    SOIL_TYPE_REQUIRED,
    WATER_SOURCE_REQUIRED,
    CURRENT_CROP_REQUIRED
}
