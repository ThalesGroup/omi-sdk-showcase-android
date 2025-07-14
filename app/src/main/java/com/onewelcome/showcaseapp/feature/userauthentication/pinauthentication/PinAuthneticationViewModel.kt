package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.PinAuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinAuthenticationViewModel @Inject() constructor(
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val pinAuthenticationUseCase: PinAuthenticationUseCase,
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.CancelAuthentication -> cancelAuthentication()
      UiEvent.StartPinAuthentication -> startPinAuthentication()
    }
  }

  private fun cancelAuthentication(): Nothing {


  }

  private fun startPinAuthentication(): Nothing {
    getRegisteredAuthenticatorsUseCase.getRegisteredAuthenticators()
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
