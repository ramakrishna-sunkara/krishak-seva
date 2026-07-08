package com.kisanalert.domain.usecase

import android.app.Activity
import com.kisanalert.core.utils.Result
import com.kisanalert.domain.model.AuthUser
import com.kisanalert.domain.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): Result<AuthUser> {
        return authRepository.signInAnonymously()
    }
}
