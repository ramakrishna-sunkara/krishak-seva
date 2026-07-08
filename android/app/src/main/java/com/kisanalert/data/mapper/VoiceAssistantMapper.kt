package com.kisanalert.data.mapper

import com.kisanalert.data.local.entity.VoiceMessageEntity
import com.kisanalert.domain.model.FarmingQuestionAnswer
import com.kisanalert.domain.model.VoiceConversationTurn

private const val FOLLOW_UP_DELIMITER: String = "|||"

fun VoiceMessageEntity.toDomain(): VoiceConversationTurn {
    return VoiceConversationTurn(
        id = id,
        question = question,
        answer = answer,
        languageCode = languageCode,
        followUpSuggestions = parseFollowUpSuggestions(followUpSuggestionsJson),
        isFromCloud = isFromCloud,
        createdAt = createdAt
    )
}

fun FarmingQuestionAnswer.toFollowUpJson(): String {
    return followUpSuggestions.joinToString(FOLLOW_UP_DELIMITER)
}

fun parseFollowUpSuggestions(followUpSuggestionsJson: String): List<String> {
    if (followUpSuggestionsJson.isBlank()) {
        return emptyList()
    }
    return followUpSuggestionsJson.split(FOLLOW_UP_DELIMITER).filter { suggestion -> suggestion.isNotBlank() }
}
