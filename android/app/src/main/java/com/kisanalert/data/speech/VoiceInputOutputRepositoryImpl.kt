package com.kisanalert.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.repository.VoiceInputOutputRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class VoiceInputOutputRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceInputOutputRepository {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTextToSpeechReady: Boolean = false
    private var isTextToSpeechInitializing: Boolean = false
    private var speechStartListener: (() -> Unit)? = null
    private var speechCompleteListener: (() -> Unit)? = null
    private var pendingSpeech: PendingSpeech? = null

    init {
        initializeTextToSpeech()
    }

    override suspend fun listenForSpeech(languageCode: String): Result<String> {
        return withContext(Dispatchers.Main) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                return@withContext Result.Error(message = "Speech recognition is not available on this device.")
            }
            suspendCancellableCoroutine<Result<String>> { continuation ->
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, mapLanguageCode(languageCode))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                val listener = object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onResults(results: Bundle?) {
                        val matches = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            .orEmpty()
                        val recognizedText = matches.firstOrNull().orEmpty().trim()
                        if (recognizedText.isNotBlank()) {
                            continuation.resume(Result.Success(recognizedText))
                        } else {
                            continuation.resume(Result.Error(message = "Could not understand speech. Please try again."))
                        }
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                    }
                    override fun onError(error: Int) {
                        val message = mapSpeechError(error)
                        continuation.resume(Result.Error(message = message))
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                    }
                }
                speechRecognizer?.setRecognitionListener(listener)
                speechRecognizer?.startListening(recognizerIntent)
                continuation.invokeOnCancellation {
                    speechRecognizer?.stopListening()
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                }
            }
        }
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun speakText(
        text: String,
        languageCode: String,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?
    ) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) {
            onComplete?.invoke()
            return
        }
        if (!isTextToSpeechReady) {
            pendingSpeech = PendingSpeech(
                text = trimmedText,
                languageCode = languageCode,
                onStart = onStart,
                onComplete = onComplete
            )
            initializeTextToSpeech()
            return
        }
        speakTextInternal(
            text = trimmedText,
            languageCode = languageCode,
            onStart = onStart,
            onComplete = onComplete
        )
    }

    override fun stopSpeaking() {
        pendingSpeech = null
        textToSpeech?.stop()
        clearSpeechListeners(invokeComplete = false)
    }

    override fun stopAllVoice() {
        stopListening()
        stopSpeaking()
    }

    private fun speakTextInternal(
        text: String,
        languageCode: String,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?
    ) {
        val locale = mapLocale(languageCode)
        textToSpeech?.language = locale
        speechStartListener = onStart
        speechCompleteListener = onComplete
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun initializeTextToSpeech() {
        if (textToSpeech != null || isTextToSpeechInitializing) {
            return
        }
        isTextToSpeechInitializing = true
        textToSpeech = TextToSpeech(context) { status ->
            isTextToSpeechInitializing = false
            isTextToSpeechReady = status == TextToSpeech.SUCCESS
            if (!isTextToSpeechReady) {
                pendingSpeech = null
                clearSpeechListeners(invokeComplete = true)
                return@TextToSpeech
            }
            val queuedSpeech = pendingSpeech
            pendingSpeech = null
            if (queuedSpeech != null) {
                speakTextInternal(
                    text = queuedSpeech.text,
                    languageCode = queuedSpeech.languageCode,
                    onStart = queuedSpeech.onStart,
                    onComplete = queuedSpeech.onComplete
                )
            }
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                speechStartListener?.invoke()
                speechStartListener = null
            }
            override fun onDone(utteranceId: String?) {
                clearSpeechListeners(invokeComplete = true)
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                clearSpeechListeners(invokeComplete = true)
            }
        })
    }

    private fun clearSpeechListeners(invokeComplete: Boolean) {
        speechStartListener = null
        if (invokeComplete) {
            speechCompleteListener?.invoke()
        }
        speechCompleteListener = null
    }

    private fun mapLanguageCode(languageCode: String): String {
        return when (languageCode) {
            "te" -> "te-IN"
            else -> "en-IN"
        }
    }

    private fun mapLocale(languageCode: String): Locale {
        return when (languageCode) {
            "te" -> Locale.forLanguageTag("te-IN")
            else -> Locale.forLanguageTag("en-IN")
        }
    }

    private fun mapSpeechError(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please try again."
            SpeechRecognizer.ERROR_CLIENT -> "Speech client error. Please retry."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is required for voice input."
            SpeechRecognizer.ERROR_NETWORK ->
                "Network error during speech recognition."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech recognition timed out."
            SpeechRecognizer.ERROR_NO_MATCH ->
                "No speech detected. Please speak clearly and try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Speech recognizer is busy. Please wait and retry."
            SpeechRecognizer.ERROR_SERVER ->
                "Speech server error. Please try again later."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "No speech heard. Tap the mic and ask your question."
            else -> "Speech recognition failed. Please try again."
        }
    }

    private data class PendingSpeech(
        val text: String,
        val languageCode: String,
        val onStart: (() -> Unit)?,
        val onComplete: (() -> Unit)?
    )
}
