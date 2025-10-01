package com.onewelcome.showcaseapp.feature.pin

import androidx.lifecycle.viewModelScope
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.NavigationEvent.PopBackStack
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent.Cancel
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent.Submit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinAuthenticationInputViewModel @Inject constructor(
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler,
) : PinViewModel() {
  init {
    listenForPinAuthenticationAttemptCounterUpdateEvent()
    listenForFinishedPinAuthenticationEvent()
    listenForPushPinAttemptCounterUpdateEvent()
    listenForFinishedPushPinAuthenticationEvent()
  }

  private fun listenForFinishedPushPinAuthenticationEvent() {
    viewModelScope.launch {
      mobileAuthWithPushPinRequestHandler.finishPinAuthenticationFlow.collect {
        _navigationEvents.send(PopBackStack)
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

  override fun onEvent(event: UiEvent) {
    when (event) {
      is Cancel -> pinAuthenticationRequestHandler.pinCallback?.denyAuthenticationRequest()
      is Submit -> pinAuthenticationRequestHandler.pinCallback?.acceptAuthenticationRequest(event.pin)
    }
  }

  private fun listenForPinAuthenticationAttemptCounterUpdateEvent() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.authenticationAttemptCounterFlow.collect {
        updateAttemptCounter(it)
      }
    }
  }

  private fun listenForFinishedPinAuthenticationEvent() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.finishPinAuthenticationFlow.collect {
        _navigationEvents.send(PopBackStack)
      }
    }
  }
}
