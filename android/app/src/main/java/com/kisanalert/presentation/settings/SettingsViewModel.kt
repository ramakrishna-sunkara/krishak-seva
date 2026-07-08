package com.kisanalert.presentation.settings

import androidx.lifecycle.viewModelScope
import com.kisanalert.BuildConfig
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.usecase.ObserveAppSettingsUseCase
import com.kisanalert.domain.usecase.SetNotificationsEnabledUseCase
import com.kisanalert.domain.usecase.SignOutUseCase
import com.kisanalert.domain.usecase.UpdatePreferredLanguageUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data object ScreenOpened : SettingsEvent
    data class NotificationPermissionResult(val isGranted: Boolean) : SettingsEvent
    data class NotificationsEnabledChanged(val isEnabled: Boolean) : SettingsEvent
    data class LanguageSelected(val language: PreferredLanguage) : SettingsEvent
    data object SignOutClicked : SettingsEvent
    data object SignOutConfirmed : SettingsEvent
    data object SignOutDismissed : SettingsEvent
    data object RecreateHandled : SettingsEvent
    data object DismissError : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val updatePreferredLanguageUseCase: UpdatePreferredLanguageUseCase,
    private val signOutUseCase: SignOutUseCase
) : MviViewModel<SettingsEvent, SettingsUiState>(
    initialState = SettingsUiState(appVersion = BuildConfig.VERSION_NAME)
) {
    private var settingsJob: Job? = null
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    init {
        onEvent(SettingsEvent.ScreenOpened)
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ScreenOpened -> loadSettings()
            is SettingsEvent.NotificationPermissionResult -> {
                setState { currentState ->
                    currentState.copy(hasNotificationPermission = event.isGranted)
                }
            }
            is SettingsEvent.NotificationsEnabledChanged -> updateNotificationsEnabled(event.isEnabled)
            is SettingsEvent.LanguageSelected -> updateLanguage(event.language)
            SettingsEvent.SignOutClicked -> {
                setState { currentState -> currentState.copy(showSignOutDialog = true) }
            }
            SettingsEvent.SignOutConfirmed -> signOut()
            SettingsEvent.SignOutDismissed -> {
                setState { currentState -> currentState.copy(showSignOutDialog = false) }
            }
            SettingsEvent.RecreateHandled -> {
                setState { currentState -> currentState.copy(shouldRecreateActivity = false) }
            }
            SettingsEvent.DismissError -> {
                setState { currentState -> currentState.copy(errorMessage = null) }
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isLoading = false)
            }
        }
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            observeAppSettingsUseCase.execute().collectLatest { settings ->
                val language = PreferredLanguage.entries.firstOrNull { item ->
                    item.code == settings.localeCode
                } ?: PreferredLanguage.TELUGU
                setState { currentState ->
                    currentState.copy(
                        areNotificationsEnabled = settings.areNotificationsEnabled,
                        preferredLanguage = language
                    )
                }
            }
        }
    }

    private fun updateNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setNotificationsEnabledUseCase.execute(isEnabled)
        }
    }

    private fun updateLanguage(language: PreferredLanguage) {
        if (language == currentState.preferredLanguage) {
            return
        }
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isUpdatingLanguage = true, errorMessage = null)
            }
            when (val result = updatePreferredLanguageUseCase.execute(language)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isUpdatingLanguage = false,
                        preferredLanguage = language,
                        shouldRecreateActivity = true
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isUpdatingLanguage = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(
                    isSigningOut = true,
                    showSignOutDialog = false,
                    errorMessage = null
                )
            }
            when (val result = signOutUseCase.execute()) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isSigningOut = false,
                        signedOut = true
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isSigningOut = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}
