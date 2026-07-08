package com.kisanalert.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for MVI (Model-View-Intent) with Jetpack Compose.
 *
 * - UI observes immutable [State] via [uiState]
 * - UI dispatches [Event] through [onEvent]
 * - ViewModel delegates business logic to domain use cases
 */
abstract class MviViewModel<Event, State>(
    initialState: State
) : ViewModel() {
    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected fun setState(reducer: (State) -> State) {
        _uiState.update(reducer)
    }

    protected val currentState: State
        get() = _uiState.value

    abstract fun onEvent(event: Event)
}
