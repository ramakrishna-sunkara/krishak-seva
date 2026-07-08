package com.kisanalert.presentation.voice

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.AiServiceErrorMapper
import com.kisanalert.core.utils.LocalFarmingQuestionAdvisor
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.usecase.AskFarmingQuestionUseCase
import com.kisanalert.domain.usecase.GetCurrentFarmerProfileUseCase
import com.kisanalert.domain.usecase.GetVoiceConversationHistoryUseCase
import com.kisanalert.domain.usecase.ListenForSpeechUseCase
import com.kisanalert.domain.usecase.SpeakAnswerUseCase
import com.kisanalert.domain.usecase.UpdatePreferredLanguageUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VoiceAssistantEvent {
    data object ScreenOpened : VoiceAssistantEvent
    data class MicPermissionResult(val isGranted: Boolean) : VoiceAssistantEvent
    data object StartListening : VoiceAssistantEvent
    data object StopListening : VoiceAssistantEvent
    data class LanguageSelected(val language: PreferredLanguage) : VoiceAssistantEvent
    data class ManualQuestionChanged(val text: String) : VoiceAssistantEvent
    data object SubmitManualQuestion : VoiceAssistantEvent
    data class SuggestionSelected(val suggestion: String) : VoiceAssistantEvent
    data class PlayAnswer(val turnId: String, val answerText: String) : VoiceAssistantEvent
    data object StopSpeaking : VoiceAssistantEvent
    data object ScreenLeft : VoiceAssistantEvent
    data object RetryLastQuestion : VoiceAssistantEvent
    data object DismissServerError : VoiceAssistantEvent
    data object RecreateHandled : VoiceAssistantEvent
    data object DismissError : VoiceAssistantEvent
}

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor(
    private val getVoiceConversationHistoryUseCase: GetVoiceConversationHistoryUseCase,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase,
    private val listenForSpeechUseCase: ListenForSpeechUseCase,
    private val askFarmingQuestionUseCase: AskFarmingQuestionUseCase,
    private val speakAnswerUseCase: SpeakAnswerUseCase,
    private val updatePreferredLanguageUseCase: UpdatePreferredLanguageUseCase
) : MviViewModel<VoiceAssistantEvent, VoiceAssistantUiState>(
    initialState = VoiceAssistantUiState()
) {
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    init {
        onEvent(VoiceAssistantEvent.ScreenOpened)
    }

    override fun onCleared() {
        stopAllVoice()
        super.onCleared()
    }

    override fun onEvent(event: VoiceAssistantEvent) {
        when (event) {
            VoiceAssistantEvent.ScreenOpened -> loadScreenData()
            is VoiceAssistantEvent.MicPermissionResult -> {
                setState { currentState ->
                    currentState.copy(hasMicPermission = event.isGranted)
                }
            }
            VoiceAssistantEvent.StartListening -> startListening()
            VoiceAssistantEvent.StopListening -> stopListening()
            is VoiceAssistantEvent.LanguageSelected -> updateLanguage(event.language)
            is VoiceAssistantEvent.ManualQuestionChanged -> {
                setState { currentState ->
                    currentState.copy(manualQuestionText = event.text, errorMessage = null)
                }
            }
            VoiceAssistantEvent.SubmitManualQuestion -> {
                processQuestion(
                    question = currentState.manualQuestionText,
                    autoSpeakAnswer = false
                )
            }
            is VoiceAssistantEvent.SuggestionSelected -> {
                processQuestion(
                    question = event.suggestion,
                    autoSpeakAnswer = false
                )
            }
            is VoiceAssistantEvent.PlayAnswer -> playAnswer(
                turnId = event.turnId,
                answerText = event.answerText
            )
            VoiceAssistantEvent.StopSpeaking -> stopSpeaking()
            VoiceAssistantEvent.ScreenLeft -> stopAllVoice()
            VoiceAssistantEvent.RetryLastQuestion -> retryLastQuestion()
            VoiceAssistantEvent.DismissServerError -> {
                setState { currentState ->
                    currentState.copy(
                        serverErrorCode = null,
                        retryableQuestion = null
                    )
                }
            }
            VoiceAssistantEvent.RecreateHandled -> {
                setState { currentState -> currentState.copy(shouldRecreateActivity = false) }
            }
            VoiceAssistantEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
        }
    }

    private fun loadScreenData() {
        viewModelScope.launch {
            setState { currentState -> currentState.copy(isLoading = true) }
            val profile = getCurrentFarmerProfileUseCase.execute()
            val language = profile?.preferredLanguage ?: PreferredLanguage.TELUGU
            when (val historyResult = getVoiceConversationHistoryUseCase.execute()) {
                is Result.Success -> {
                    val history = historyResult.data
                    val cropName = profile?.currentCrop.orEmpty().ifBlank { "your crop" }
                    val suggestions = if (history.isEmpty()) {
                        LocalFarmingQuestionAdvisor.buildStarterSuggestions(
                            language = language,
                            cropName = cropName
                        )
                    } else {
                        emptyList()
                    }
                    setState { currentState ->
                        currentState.copy(
                            isLoading = false,
                            farmerName = profile?.name.orEmpty(),
                            currentCrop = profile?.currentCrop.orEmpty(),
                            selectedLanguage = language,
                            conversationHistory = history,
                            starterSuggestions = suggestions
                        )
                    }
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        farmerName = profile?.name.orEmpty(),
                        currentCrop = profile?.currentCrop.orEmpty(),
                        selectedLanguage = language,
                        starterSuggestions = buildStarterSuggestionsIfNeeded(
                            language = language,
                            cropName = profile?.currentCrop.orEmpty(),
                            hasHistory = false
                        ),
                        errorMessage = historyResult.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun startListening() {
        if (!currentState.hasMicPermission) {
            setState { currentState ->
                currentState.copy(errorMessage = "Microphone permission is required.")
            }
            return
        }
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isListening = true, errorMessage = null)
            }
            when (val result = listenForSpeechUseCase.execute(currentState.selectedLanguage)) {
                is Result.Success -> {
                    setState { currentState -> currentState.copy(isListening = false) }
                    processQuestion(
                        question = result.data,
                        autoSpeakAnswer = true
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isListening = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun stopListening() {
        listenForSpeechUseCase.stop()
        setState { currentState -> currentState.copy(isListening = false) }
    }

    private fun updateLanguage(language: PreferredLanguage) {
        if (language == currentState.selectedLanguage) {
            return
        }
        setState { currentState ->
            currentState.copy(selectedLanguage = language, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = updatePreferredLanguageUseCase.execute(language)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        shouldRecreateActivity = true,
                        starterSuggestions = buildStarterSuggestionsIfNeeded(
                            language = language,
                            cropName = currentState.currentCrop,
                            hasHistory = currentState.conversationHistory.isNotEmpty()
                        )
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(errorMessage = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun processQuestion(question: String, autoSpeakAnswer: Boolean) {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(
                    isProcessing = true,
                    errorMessage = null,
                    manualQuestionText = "",
                    speakingTurnId = null,
                    preparingTurnId = null,
                    serverErrorCode = null,
                    retryableQuestion = null
                )
            }
            val language = currentState.selectedLanguage
            when (val result = askFarmingQuestionUseCase.execute(question, language)) {
                is Result.Success -> {
                    val turn = result.data
                    if (autoSpeakAnswer) {
                        playAnswer(turnId = turn.id, answerText = turn.answer)
                    }
                    setState { currentState ->
                        currentState.copy(
                            isProcessing = false,
                            latestTurn = turn,
                            starterSuggestions = emptyList(),
                            conversationHistory = listOf(turn) + currentState.conversationHistory.filter { item ->
                                item.id != turn.id
                            },
                            isOfflineMode = !turn.isFromCloud,
                            serverErrorCode = null,
                            retryableQuestion = null
                        )
                    }
                }
                is Result.Error -> {
                    val errorCode = AiServiceErrorMapper.resolveErrorCode(
                        exception = result.exception,
                        fallbackMessage = result.message
                    )
                    setState { currentState ->
                        currentState.copy(
                            isProcessing = false,
                            serverErrorCode = errorCode,
                            retryableQuestion = question,
                            retryAutoSpeakAnswer = autoSpeakAnswer,
                            errorMessage = if (AiServiceErrorMapper.isRetryable(errorCode)) {
                                null
                            } else {
                                result.message
                            }
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun retryLastQuestion() {
        val question = currentState.retryableQuestion ?: return
        processQuestion(
            question = question,
            autoSpeakAnswer = currentState.retryAutoSpeakAnswer
        )
    }

    private fun playAnswer(turnId: String, answerText: String) {
        val trimmedAnswer = answerText.trim()
        if (trimmedAnswer.isBlank()) {
            return
        }
        if (currentState.speakingTurnId == turnId || currentState.preparingTurnId == turnId) {
            stopSpeaking()
            return
        }
        speakAnswerUseCase.stop()
        setState { currentState ->
            currentState.copy(
                preparingTurnId = turnId,
                speakingTurnId = null
            )
        }
        speakAnswerUseCase.execute(
            text = trimmedAnswer,
            language = currentState.selectedLanguage,
            onStart = {
                setState { currentState ->
                    if (currentState.preparingTurnId == turnId) {
                        currentState.copy(
                            preparingTurnId = null,
                            speakingTurnId = turnId
                        )
                    } else {
                        currentState
                    }
                }
            },
            onComplete = {
                setState { currentState ->
                    if (currentState.speakingTurnId == turnId || currentState.preparingTurnId == turnId) {
                        currentState.copy(
                            speakingTurnId = null,
                            preparingTurnId = null
                        )
                    } else {
                        currentState
                    }
                }
            }
        )
    }

    private fun stopSpeaking() {
        speakAnswerUseCase.stop()
        setState { currentState ->
            currentState.copy(
                speakingTurnId = null,
                preparingTurnId = null
            )
        }
    }

    private fun stopAllVoice() {
        listenForSpeechUseCase.stopAll()
        setState { currentState ->
            currentState.copy(
                isListening = false,
                speakingTurnId = null,
                preparingTurnId = null
            )
        }
    }

    private fun buildStarterSuggestionsIfNeeded(
        language: PreferredLanguage,
        cropName: String,
        hasHistory: Boolean
    ): List<String> {
        if (hasHistory) {
            return emptyList()
        }
        val resolvedCropName = cropName.ifBlank { "your crop" }
        return LocalFarmingQuestionAdvisor.buildStarterSuggestions(
            language = language,
            cropName = resolvedCropName
        )
    }
}
