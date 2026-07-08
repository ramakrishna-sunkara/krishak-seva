package com.kisanalert

import android.app.Application
import com.kisanalert.core.firebase.FirebaseAppCheckInitializer
import com.kisanalert.core.locale.AppLocaleInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KrishakSevaApplication : Application() {
    @Inject
    lateinit var appLocaleInitializer: AppLocaleInitializer

    override fun onCreate() {
        FirebaseAppCheckInitializer.install()
        super.onCreate()
        appLocaleInitializer.initialize()
    }
}
