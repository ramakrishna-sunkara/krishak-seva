package com.kisanalert.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.PhoneVerificationResult
import com.kisanalert.domain.model.PreferredLanguage
import com.kisanalert.domain.usecase.ObserveAppSettingsUseCase
import com.kisanalert.domain.usecase.SendPhoneVerificationCodeUseCase
import com.kisanalert.domain.usecase.SetAppLocaleUseCase
import com.kisanalert.domain.usecase.SignInAnonymouslyUseCase
import com.kisanalert.domain.usecase.SyncAuthSessionUseCase
import com.kisanalert.domain.usecase.VerifyPhoneCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val sendPhoneVerificationCodeUseCase: SendPhoneVerificationCodeUseCase,
    private val verifyPhoneCodeUseCase: VerifyPhoneCodeUseCase,
    private val syncAuthSessionUseCase: SyncAuthSessionUseCase,
    private val setAppLocaleUseCase: SetAppLocaleUseCase,
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase
) : ViewModel() {
    private val _uiState: MutableStateFlow<AuthUiState> = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val languageOptions: List<PreferredLanguage> = PreferredLanguage.entries

    init {
        observeAppLanguage()
    }

    private fun observeAppLanguage() {
        viewModelScope.launch {
            observeAppSettingsUseCase.execute().collectLatest { settings ->
                val language = PreferredLanguage.entries.firstOrNull { language ->
                    language.code == settings.localeCode
                } ?: PreferredLanguage.TELUGU
                _uiState.update { currentState ->
                    currentState.copy(selectedLanguage = language)
                }
            }
        }
    }

    fun onLanguageSelected(language: PreferredLanguage) {
        if (language == _uiState.value.selectedLanguage) {
            return
        }
        viewModelScope.launch {
            setAppLocaleUseCase.execute(language)
            _uiState.update { currentState ->
                currentState.copy(
                    selectedLanguage = language,
                    shouldRecreateActivity = true
                )
            }
        }
    }

    fun onRecreateHandled() {
        _uiState.update { currentState ->
            currentState.copy(shouldRecreateActivity = false)
        }
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        val digitsOnly = phoneNumber.filter { character -> character.isDigit() }.take(10)
        _uiState.update { currentState ->
            currentState.copy(
                phoneNumber = digitsOnly,
                errorMessage = null
            )
        }
    }

    fun onOtpCodeChanged(otpCode: String) {
        val digitsOnly = otpCode.filter { character -> character.isDigit() }.take(6)
        _uiState.update { currentState ->
            currentState.copy(
                otpCode = digitsOnly,
                errorMessage = null
            )
        }
    }

    fun onSignInAnonymously() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            when (val result = signInAnonymouslyUseCase.execute()) {
                is Result.Success -> navigateAfterAuthentication()
                is Result.Error -> _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onSendOtp(activity: Activity) {
        val phoneNumber = _uiState.value.phoneNumber
        if (phoneNumber.length != 10) {
            _uiState.update { currentState ->
                currentState.copy(errorMessage = "Enter a valid 10-digit mobile number.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            when (val result = sendPhoneVerificationCodeUseCase.execute(activity, phoneNumber)) {
                is Result.Success -> handlePhoneVerificationResult(result.data)
                is Result.Error -> _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onVerifyOtp() {
        val verificationId = _uiState.value.verificationId
        val otpCode = _uiState.value.otpCode
        if (verificationId.isNullOrBlank()) {
            _uiState.update { currentState ->
                currentState.copy(errorMessage = "Request OTP before verifying.")
            }
            return
        }
        if (otpCode.length < 6) {
            _uiState.update { currentState ->
                currentState.copy(errorMessage = "Enter the 6-digit OTP.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            when (val result = verifyPhoneCodeUseCase.execute(verificationId, otpCode)) {
                is Result.Success -> navigateAfterAuthentication()
                is Result.Error -> _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onBackToPhoneEntry() {
        _uiState.update { currentState ->
            currentState.copy(
                authStep = AuthStep.Welcome,
                otpCode = "",
                verificationId = null,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun onDismissError() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    private suspend fun handlePhoneVerificationResult(result: PhoneVerificationResult) {
        when (result) {
            is PhoneVerificationResult.AutoVerified -> navigateAfterAuthentication()
            is PhoneVerificationResult.CodeSent -> _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    authStep = AuthStep.PhoneOtp,
                    verificationId = result.verificationId,
                    infoMessage = "OTP sent to +91${currentState.phoneNumber}"
                )
            }
        }
    }

    private suspend fun navigateAfterAuthentication() {
        val sessionState = syncAuthSessionUseCase.execute()
        val destination = if (sessionState.isOnboardingComplete) {
            AuthDestination.Dashboard
        } else {
            AuthDestination.FarmerRegistration
        }
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                destination = destination
            )
        }
    }
}
