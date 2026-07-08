package com.kisanalert.domain.model

data class VoiceConversationTurn(
    val id: String,
    val question: String,
    val answer: String,
    val languageCode: String,
    val followUpSuggestions: List<String> = emptyList(),
    val isFromCloud: Boolean,
    val createdAt: Long
)

data class FarmingQuestionRequest(
    val userId: String,
    val question: String,
    val languageCode: String,
    val farmerName: String,
    val village: String,
    val district: String,
    val state: String,
    val currentCrop: String,
    val soilType: String,
    val waterSource: String
)

data class FarmingQuestionAnswer(
    val answer: String,
    val followUpSuggestions: List<String>,
    val isFromCloud: Boolean = true
)
