package com.kisanalert.data.remote.firebase

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.kisanalert.core.constants.AppConstants
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    suspend fun uploadCropScanImage(
        userId: String,
        scanId: String,
        imageBytes: ByteArray
    ): String {
        return withTimeout(AppConstants.STORAGE_UPLOAD_TIMEOUT_MS) {
            val storagePath = "crop_scans/$userId/$scanId.jpg"
            Log.d(TAG, "Uploading crop scan image path=$storagePath bytes=${imageBytes.size}")
            val reference = firebaseStorage.reference.child(storagePath)
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            reference.putBytes(imageBytes, metadata).await()
            val downloadUrl = reference.downloadUrl.await().toString()
            Log.d(TAG, "Crop scan image uploaded path=$storagePath url=$downloadUrl")
            downloadUrl
        }
    }

    companion object {
        private const val TAG: String = "FirebaseStorage"
    }
}
