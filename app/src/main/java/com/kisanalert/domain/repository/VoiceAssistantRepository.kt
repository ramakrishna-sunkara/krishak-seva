package com.kisanalert.domain.repository

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.FarmingQuestionRequest
import com.kisanalert.domain.model.VoiceConversationTurn

interface VoiceAssistantRepository {
    suspend fun getConversationHistory(userId: String): List<VoiceConversationTurn>
    suspend fun askFarmingQuestion(request: FarmingQuestionRequest): Result<FarmingQuestionAnswer>
    suspend fun saveConversationTurn(
        userId: String,
        question: String,
        answer: FarmingQuestionAnswer,
        languageCode: String
    ): VoiceConversationTurn
}
