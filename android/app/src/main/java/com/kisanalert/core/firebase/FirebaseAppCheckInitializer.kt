package com.kisanalert.core.firebase

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.kisanalert.BuildConfig

object FirebaseAppCheckInitializer {
    private const val TAG: String = "FirebaseAppCheck"

    fun install() {
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            firebaseAppCheck.getAppCheckToken(false)
                .addOnSuccessListener { tokenResult ->
                    Log.d(TAG, "Debug App Check token: ${tokenResult.token}")
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Failed to fetch debug App Check token.", exception)
                }
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
