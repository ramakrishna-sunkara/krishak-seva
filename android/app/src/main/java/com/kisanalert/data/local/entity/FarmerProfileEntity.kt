package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_profiles")
data class FarmerProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val pincode: String,
    val village: String,
    val district: String,
    val state: String,
    val farmSizeAcres: Double,
    val soilType: String,
    val waterSource: String,
    val preferredLanguage: String,
    val currentCrop: String,
    val latitude: Double?,
    val longitude: Double?,
    val updatedAt: Long,
    val isSynced: Boolean
)
