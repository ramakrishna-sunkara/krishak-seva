package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.FarmerProfileEntity
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource

fun FarmerProfile.toEntity(): FarmerProfileEntity {
    return FarmerProfileEntity(
        userId = userId,
        name = name,
        pincode = pincode,
        village = village,
        district = district,
        state = state,
        farmSizeAcres = farmSizeAcres,
        soilType = soilType.name,
        waterSource = waterSource.name,
        preferredLanguage = preferredLanguage.code,
        currentCrop = currentCrop,
        latitude = latitude,
        longitude = longitude,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}

fun FarmerProfileEntity.toDomain(): FarmerProfile {
    return FarmerProfile(
        userId = userId,
        name = name,
        pincode = pincode,
        village = village,
        district = district,
        state = state,
        farmSizeAcres = farmSizeAcres,
        soilType = SoilType.valueOf(soilType),
        waterSource = WaterSource.valueOf(waterSource),
        preferredLanguage = PreferredLanguage.entries.first { language ->
            language.code == preferredLanguage
        },
        currentCrop = currentCrop,
        latitude = latitude,
        longitude = longitude,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
