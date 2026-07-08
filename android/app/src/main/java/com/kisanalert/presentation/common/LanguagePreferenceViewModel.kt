package com.kisanalert.presentation.common

import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.usecase.ObserveAppSettingsUseCase
import com.kisanalert.domain.usecase.UpdatePreferredLanguageUseCase
import com.kisanalert.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LanguagePreferenceEvent {
    data class LanguageSelected(val language: PreferredLanguage) : LanguagePreferenceEvent
    data object RecreateHandled : LanguagePreferenceEvent
}

data class LanguagePreferenceUiState(
    val selectedLanguage: PreferredLanguage = PreferredLanguage.TELUGU,
    val isUpdating: Boolean = false,
    val shouldRecreateActivity: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LanguagePreferenceViewModel @Inject constructor(
    private val updatePreferredLanguageUseCase: UpdatePreferredLanguageUseCase,
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase
) : MviViewModel<LanguagePreferenceEvent, LanguagePreferenceUiState>(
    initialState = LanguagePreferenceUiState()
) {
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    init {
        observeLanguage()
    }

    override fun onEvent(event: LanguagePreferenceEvent) {
        when (event) {
            is LanguagePreferenceEvent.LanguageSelected -> updateLanguage(event.language)
            LanguagePreferenceEvent.RecreateHandled -> {
                setState { currentState ->
                    currentState.copy(shouldRecreateActivity = false)
                }
            }
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            observeAppSettingsUseCase.execute().collectLatest { settings ->
                val language = PreferredLanguage.entries.firstOrNull { item ->
                    item.code == settings.localeCode
                } ?: PreferredLanguage.TELUGU
                setState { currentState ->
                    currentState.copy(selectedLanguage = language)
                }
            }
        }
    }

    private fun updateLanguage(language: PreferredLanguage) {
        if (language == currentState.selectedLanguage) {
            return
        }
        viewModelScope.launch {
            setState { currentState ->
                currentState.copy(isUpdating = true, errorMessage = null)
            }
            when (val result = updatePreferredLanguageUseCase.execute(language)) {
                is Result.Success -> setState { currentState ->
                    currentState.copy(
                        isUpdating = false,
                        selectedLanguage = language,
                        shouldRecreateActivity = true
                    )
                }
                is Result.Error -> setState { currentState ->
                    currentState.copy(
                        isUpdating = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}
