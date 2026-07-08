package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result

interface VoiceInputOutputRepository {
    suspend fun listenForSpeech(languageCode: String): Result<String>
    fun stopListening()
    fun speakText(
        text: String,
        languageCode: String,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    )
    fun stopSpeaking()
    fun stopAllVoice()
}
