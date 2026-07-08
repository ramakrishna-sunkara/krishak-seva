package com.kisanalert.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenDataSource @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun fetchToken(): String? {
        return try {
            firebaseMessaging.token.await()
        } catch (exception: Exception) {
            null
        }
    }

    suspend fun saveToken(userId: String, token: String) {
        try {
            firebaseFirestore.collection(FCM_TOKENS_COLLECTION)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "token" to token,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (exception: Exception) {
            // Best-effort sync; missing Firestore rules must not crash the app.
        }
    }

    companion object {
        private const val FCM_TOKENS_COLLECTION: String = "fcm_tokens"
    }
}
