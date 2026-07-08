package com.kisanalert.presentation.voice

import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.VoiceConversationTurn

data class VoiceAssistantUiState(
    val isLoading: Boolean = true,
    val hasMicPermission: Boolean = false,
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val selectedLanguage: PreferredLanguage = PreferredLanguage.TELUGU,
    val farmerName: String = "",
    val currentCrop: String = "",
    val conversationHistory: List<VoiceConversationTurn> = emptyList(),
    val latestTurn: VoiceConversationTurn? = null,
    val starterSuggestions: List<String> = emptyList(),
    val manualQuestionText: String = "",
    val errorMessage: String? = null,
    val shouldRecreateActivity: Boolean = false,
    val isOfflineMode: Boolean = false,
    val speakingTurnId: String? = null,
    val preparingTurnId: String? = null,
    val serverErrorCode: String? = null,
    val retryableQuestion: String? = null,
    val retryAutoSpeakAnswer: Boolean = false
)
