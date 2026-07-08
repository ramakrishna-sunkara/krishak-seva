package com.kisanalert

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kisanalert.core.notification.KisanNotificationHelper
import com.kisanalert.core.ui.theme.KisanAlertTheme
import com.kisanalert.domain.repository.VoiceInputOutputRepository
import com.kisanalert.presentation.navigation.KisanNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var notificationHelper: KisanNotificationHelper

    @Inject
    lateinit var voiceInputOutputRepository: VoiceInputOutputRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationHelper.createNotificationChannels()
        setContent {
            KisanAlertTheme {
                KisanNavHost()
            }
        }
    }

    override fun onStop() {
        voiceInputOutputRepository.stopAllVoice()
        super.onStop()
    }
}
