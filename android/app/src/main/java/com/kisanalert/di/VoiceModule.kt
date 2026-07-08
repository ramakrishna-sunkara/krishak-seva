package com.kisanalert.di

import com.kisanalert.data.repository.VoiceAssistantRepositoryImpl
import com.kisanalert.data.speech.VoiceInputOutputRepositoryImpl
import com.kisanalert.domain.repository.VoiceAssistantRepository
import com.kisanalert.domain.repository.VoiceInputOutputRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {
    @Binds
    @Singleton
    abstract fun bindVoiceAssistantRepository(
        voiceAssistantRepositoryImpl: VoiceAssistantRepositoryImpl
    ): VoiceAssistantRepository

    @Binds
    @Singleton
    abstract fun bindVoiceInputOutputRepository(
        voiceInputOutputRepositoryImpl: VoiceInputOutputRepositoryImpl
    ): VoiceInputOutputRepository
}
