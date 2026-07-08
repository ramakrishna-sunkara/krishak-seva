package com.kisanalert.data.repository

import com.kisanalert.core.utils.LocalFarmingQuestionAdvisor
import com.kisanalert.core.utils.Result
import com.kisanalert.data.local.dao.VoiceMessageDao
import com.kisanalert.data.mapper.toDomain
import com.kisanalert.data.mapper.toFollowUpJson
import com.kisanalert.data.local.entity.VoiceMessageEntity
import com.kisanalert.data.remote.firebase.CloudFunctionsDataSource
import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.FarmingQuestionRequest
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.VoiceConversationTurn
import com.kisanalert.domain.repository.VoiceAssistantRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceAssistantRepositoryImpl @Inject constructor(
    private val voiceMessageDao: VoiceMessageDao,
    private val cloudFunctionsDataSource: CloudFunctionsDataSource
) : VoiceAssistantRepository {
    override suspend fun getConversationHistory(userId: String): List<VoiceConversationTurn> {
        return voiceMessageDao.getRecentMessages(userId).map { entity -> entity.toDomain() }
    }

    override suspend fun askFarmingQuestion(
        request: FarmingQuestionRequest
    ): Result<FarmingQuestionAnswer> {
        return try {
            val answer = fetchAnswerWithFallback(request)
            Result.Success(answer)
        } catch (exception: Exception) {
            Result.Error(
                message = exception.localizedMessage ?: "Failed to get farming advice.",
                exception = exception
            )
        }
    }

    override suspend fun saveConversationTurn(
        userId: String,
        question: String,
        answer: FarmingQuestionAnswer,
        languageCode: String
    ): VoiceConversationTurn {
        val turn = VoiceConversationTurn(
            id = UUID.randomUUID().toString(),
            question = question,
            answer = answer.answer,
            languageCode = languageCode,
            followUpSuggestions = answer.followUpSuggestions,
            isFromCloud = answer.isFromCloud,
            createdAt = System.currentTimeMillis()
        )
        voiceMessageDao.insertMessage(
            VoiceMessageEntity(
                id = turn.id,
                userId = userId,
                question = turn.question,
                answer = turn.answer,
                languageCode = turn.languageCode,
                followUpSuggestionsJson = answer.toFollowUpJson(),
                isFromCloud = turn.isFromCloud,
                createdAt = turn.createdAt
            )
        )
        return turn
    }

    private suspend fun fetchAnswerWithFallback(
        request: FarmingQuestionRequest
    ): FarmingQuestionAnswer {
        return try {
            cloudFunctionsDataSource.askFarmingQuestion(request)
        } catch (exception: Exception) {
            val language = PreferredLanguage.entries.firstOrNull { language ->
                language.code == request.languageCode
            } ?: PreferredLanguage.TELUGU
            LocalFarmingQuestionAdvisor.buildOfflineAnswer(
                question = request.question,
                cropName = request.currentCrop,
                language = language
            )
        }
    }
}
