package com.onewelcome.showcaseapp.feature.pin

import androidx.lifecycle.viewModelScope
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PushWithPinConfirmationInputViewModel @Inject constructor(
  private val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler,
) : PinViewModel() {

  init {
    listenForPushPinAttemptCounterUpdateEvent()
    listenForFinishedPushPinAuthenticationEvent()
  }

  override fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.Submit -> mobileAuthWithPushPinRequestHandler.pinCallback?.acceptAuthenticationRequest(event.pin)
      is UiEvent.Cancel -> mobileAuthWithPushPinRequestHandler.pinCallback?.denyAuthenticationRequest()
    }
  }

  private fun listenForFinishedPushPinAuthenticationEvent() {
    viewModelScope.launch {
      mobileAuthWithPushPinRequestHandler.finishPinAuthenticationFlow.collect {
        _navigationEvents.send(NavigationEvent.NavigateToTransactionConfirmationResult)
      }
    }
  }

  private fun listenForPushPinAttemptCounterUpdateEvent() {
    viewModelScope.launch {
      mobileAuthWithPushPinRequestHandler.authenticationAttemptCounterFlow.collect {
        updateAttemptCounter(it)
      }
    }
  }

  private fun updateAttemptCounter(counter: AuthenticationAttemptCounter) {
    val shouldShowErrorMessage = counter.failedAttempts > 0
    uiState = if (shouldShowErrorMessage) {
      uiState.copy(authenticationAttemptCounter = counter, pinValidationError = "Wrong PIN, try again")
    } else {
      uiState.copy(authenticationAttemptCounter = counter)
    }
  }
}
