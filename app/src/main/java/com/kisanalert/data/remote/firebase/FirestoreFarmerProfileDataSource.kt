package com.kisanalert.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kisanalert.core.constants.FirestoreCollections
import com.kisanalert.domain.model.FarmerProfile
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.SoilType
import com.kisanalert.domain.model.WaterSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreFarmerProfileDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveProfile(profile: FarmerProfile) {
        firestore.collection(FirestoreCollections.FARMER_PROFILES)
            .document(profile.userId)
            .set(profile.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun getProfile(userId: String): FarmerProfile? {
        val snapshot = firestore.collection(FirestoreCollections.FARMER_PROFILES)
            .document(userId)
            .get()
            .await()
        if (!snapshot.exists()) {
            return null
        }
        val data = snapshot.data ?: return null
        return data.toFarmerProfile(userId = userId)
    }

    private fun FarmerProfile.toFirestoreMap(): Map<String, Any> {
        val fields = mutableMapOf<String, Any>(
            "userId" to userId,
            "name" to name,
            "pincode" to pincode,
            "village" to village,
            "district" to district,
            "state" to state,
            "farmSizeAcres" to farmSizeAcres,
            "soilType" to soilType.name,
            "waterSource" to waterSource.name,
            "preferredLanguage" to preferredLanguage.code,
            "currentCrop" to currentCrop,
            "updatedAt" to updatedAt
        )
        latitude?.let { value -> fields["latitude"] = value }
        longitude?.let { value -> fields["longitude"] = value }
        return fields
    }

    private fun Map<String, Any>.toFarmerProfile(userId: String): FarmerProfile {
        return FarmerProfile(
            userId = userId,
            name = this["name"] as? String ?: "",
            pincode = this["pincode"] as? String ?: "",
            village = this["village"] as? String ?: "",
            district = this["district"] as? String ?: "",
            state = this["state"] as? String ?: "",
            farmSizeAcres = (this["farmSizeAcres"] as? Number)?.toDouble() ?: 0.0,
            soilType = SoilType.valueOf(this["soilType"] as? String ?: SoilType.LOAMY.name),
            waterSource = WaterSource.valueOf(this["waterSource"] as? String ?: WaterSource.RAIN_FED.name),
            preferredLanguage = PreferredLanguage.entries.first { language ->
                language.code == (this["preferredLanguage"] as? String ?: PreferredLanguage.TELUGU.code)
            },
            currentCrop = this["currentCrop"] as? String ?: "",
            latitude = (this["latitude"] as? Number)?.toDouble(),
            longitude = (this["longitude"] as? Number)?.toDouble(),
            updatedAt = (this["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isSynced = true
        )
    }
}
