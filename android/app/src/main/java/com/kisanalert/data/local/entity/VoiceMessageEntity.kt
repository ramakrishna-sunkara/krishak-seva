package com.kisanalert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_messages")
data class VoiceMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val question: String,
    val answer: String,
    val languageCode: String,
    val followUpSuggestionsJson: String,
    val isFromCloud: Boolean,
    val createdAt: Long
)
