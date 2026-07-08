package com.kisanalert.core.locale

import com.kisanalert.domain.usecase.InitializeAppLocaleUseCase
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLocaleInitializer @Inject constructor(
    private val initializeAppLocaleUseCase: InitializeAppLocaleUseCase
) {
    fun initialize() {
        runBlocking {
            initializeAppLocaleUseCase.execute()
        }
    }
}
