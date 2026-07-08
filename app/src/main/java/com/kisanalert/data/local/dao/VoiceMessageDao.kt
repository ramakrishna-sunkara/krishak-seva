package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.VoiceMessageEntity

@Dao
interface VoiceMessageDao {
    @Query(
        "SELECT * FROM voice_messages WHERE userId = :userId ORDER BY createdAt DESC LIMIT 30"
    )
    suspend fun getRecentMessages(userId: String): List<VoiceMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: VoiceMessageEntity)
}
