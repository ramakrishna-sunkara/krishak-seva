package com.kisanalert.domain.usecase

import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.FarmingQuestionRequest
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.model.VoiceConversationTurn
import com.kisanalert.domain.repository.VoiceAssistantRepository
import com.kisanalert.domain.repository.VoiceInputOutputRepository
import javax.inject.Inject

class GetVoiceConversationHistoryUseCase @Inject constructor(
    private val voiceAssistantRepository: VoiceAssistantRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend fun execute(): Result<List<VoiceConversationTurn>> {
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to use Voice Assistant.")
        return Result.Success(voiceAssistantRepository.getConversationHistory(userId))
    }
}

class AskFarmingQuestionUseCase @Inject constructor(
    private val voiceAssistantRepository: VoiceAssistantRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase
) {
    suspend fun execute(
        question: String,
        language: PreferredLanguage
    ): Result<VoiceConversationTurn> {
        val trimmedQuestion = question.trim()
        if (trimmedQuestion.isBlank()) {
            return Result.Error(message = "Please ask a farming question.")
        }
        val userId = getCurrentUserIdUseCase.execute()
            ?: return Result.Error(message = "Please sign in to use Voice Assistant.")
        val profile = getCurrentFarmerProfileUseCase.execute()
            ?: return Result.Error(message = "Complete farmer registration before using Voice Assistant.")
        val request = FarmingQuestionRequest(
            userId = userId,
            question = trimmedQuestion,
            languageCode = language.code,
            farmerName = profile.name,
            village = profile.village,
            district = profile.district,
            state = profile.state,
            currentCrop = profile.currentCrop,
            soilType = profile.soilType.name,
            waterSource = profile.waterSource.name
        )
        return when (val answerResult = voiceAssistantRepository.askFarmingQuestion(request)) {
            is Result.Success -> {
                val turn = voiceAssistantRepository.saveConversationTurn(
                    userId = userId,
                    question = trimmedQuestion,
                    answer = answerResult.data,
                    languageCode = language.code
                )
                Result.Success(turn)
            }
            is Result.Error -> answerResult
            is Result.Loading -> Result.Error(message = "Question is still processing.")
        }
    }
}

class ListenForSpeechUseCase @Inject constructor(
    private val voiceInputOutputRepository: VoiceInputOutputRepository
) {
    suspend fun execute(language: PreferredLanguage): Result<String> {
        return voiceInputOutputRepository.listenForSpeech(languageCode = language.code)
    }

    fun stop() {
        voiceInputOutputRepository.stopListening()
    }

    fun stopAll() {
        voiceInputOutputRepository.stopAllVoice()
    }
}

class SpeakAnswerUseCase @Inject constructor(
    private val voiceInputOutputRepository: VoiceInputOutputRepository
) {
    fun execute(
        text: String,
        language: PreferredLanguage,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        voiceInputOutputRepository.speakText(
            text = text,
            languageCode = language.code,
            onStart = onStart,
            onComplete = onComplete
        )
    }

    fun stop() {
        voiceInputOutputRepository.stopSpeaking()
    }

    fun stopAll() {
        voiceInputOutputRepository.stopAllVoice()
    }
}
