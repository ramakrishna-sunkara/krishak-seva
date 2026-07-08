package com.kisanalert.domain.usecase

import com.kisanalert.core.locale.LocaleManager
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AppSettings
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.repository.AppSettingsRepository
import com.kisanalert.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    fun execute(): Flow<AppSettings> {
        return appSettingsRepository.observeSettings()
    }
}

class SetNotificationsEnabledUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend fun execute(isEnabled: Boolean) {
        appSettingsRepository.setNotificationsEnabled(isEnabled)
    }
}

class SetAppLocaleUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend fun execute(language: PreferredLanguage) {
        appSettingsRepository.setLocaleCode(language.code)
        LocaleManager.applyApplicationLocale(language.code)
    }
}

class InitializeAppLocaleUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase
) {
    suspend fun execute() {
        val profile = getCurrentFarmerProfileUseCase.execute()
        val settings = appSettingsRepository.getSettings()
        val localeCode = LocaleManager.resolveLocaleCode(
            profileLanguageCode = profile?.preferredLanguage?.code,
            storedLocaleCode = settings.localeCode
        )
        appSettingsRepository.setLocaleCode(localeCode)
        LocaleManager.applyApplicationLocale(localeCode)
    }
}

class GetPreferredLanguageUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase
) {
    suspend fun execute(): PreferredLanguage {
        val profile = getCurrentFarmerProfileUseCase.execute()
        val settings = appSettingsRepository.getSettings()
        val localeCode = LocaleManager.resolveLocaleCode(
            profileLanguageCode = profile?.preferredLanguage?.code,
            storedLocaleCode = settings.localeCode
        )
        return LocaleManager.toPreferredLanguage(localeCode)
    }
}

class MarkDashboardGuideSeenUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend fun execute() {
        appSettingsRepository.setDashboardGuideSeen(hasSeenGuide = true)
    }
}

class UpdatePreferredLanguageUseCase @Inject constructor(
    private val getCurrentFarmerProfileUseCase: GetCurrentFarmerProfileUseCase,
    private val saveFarmerProfileUseCase: SaveFarmerProfileUseCase,
    private val setAppLocaleUseCase: SetAppLocaleUseCase
) {
    suspend fun execute(language: PreferredLanguage): Result<Unit> {
        setAppLocaleUseCase.execute(language)
        val profile = getCurrentFarmerProfileUseCase.execute()
        if (profile == null) {
            return Result.Success(Unit)
        }
        return when (val result = saveFarmerProfileUseCase.execute(profile.copy(preferredLanguage = language))) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(message = result.message, exception = result.exception)
            is Result.Loading -> Result.Loading
        }
    }
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): Result<Unit> {
        return authRepository.signOut()
    }
}
