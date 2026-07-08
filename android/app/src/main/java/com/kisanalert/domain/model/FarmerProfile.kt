package com.kisanalert.domain.model

enum class SoilType(val displayName: String) {
    LOAMY(displayName = "Loamy"),
    CLAY(displayName = "Clay"),
    SANDY(displayName = "Sandy"),
    RED(displayName = "Red Soil"),
    BLACK(displayName = "Black Soil"),
    ALLUVIAL(displayName = "Alluvial")
}

enum class WaterSource(val displayName: String) {
    BOREWELL(displayName = "Borewell"),
    CANAL(displayName = "Canal"),
    RAIN_FED(displayName = "Rain-fed"),
    DRIP(displayName = "Drip Irrigation"),
    SPRINKLER(displayName = "Sprinkler"),
    RIVER(displayName = "River / Tank")
}

data class FarmerProfile(
    val userId: String,
    val name: String,
    val pincode: String = "",
    val village: String,
    val district: String,
    val state: String,
    val farmSizeAcres: Double,
    val soilType: SoilType,
    val waterSource: WaterSource,
    val preferredLanguage: PreferredLanguage,
    val currentCrop: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
