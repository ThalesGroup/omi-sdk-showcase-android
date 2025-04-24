package com.onewelcome.showcaseapp.feature.userregistration.browserregistration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BrowserRegistrationViewModel : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartBrowserRegistration -> register()
    }
  }

  private fun register() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      delay(1000)
      uiState = uiState.copy(isLoading = false)
    }
  }

  data class State(
    val isLoading: Boolean = false,
    val result: Result<Unit, OneginiRegistrationError>? = null
  )

  sealed interface UiEvent {
    data object StartBrowserRegistration : UiEvent
  }
}