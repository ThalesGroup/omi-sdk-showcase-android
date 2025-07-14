package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinAuthenticationViewModel @Inject() constructor() : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  fun onEvent(value: UiEvent) {

  }

  data class State(
    val result: Result<Void, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val userProfileIds: List<String> = emptyList(),
    val isAuthenticationCancellationEnabled: Boolean = false,
  )

  sealed interface UiEvent {
    data object StartPinAuthentication : UiEvent
    data object CancelAuthentication : UiEvent
  }
}
