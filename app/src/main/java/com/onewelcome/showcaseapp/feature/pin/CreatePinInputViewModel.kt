package com.onewelcome.showcaseapp.feature.pin

import androidx.lifecycle.viewModelScope
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.NavigationEvent.PopBackStack
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent.Cancel
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent.Submit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePinInputViewModel @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler,
) : PinViewModel() {

  init {
    uiState = uiState.copy(maxPinLength = createPinRequestHandler.maxPinLength)
    listenForPinFinishedEvent()
    listenForPinValidationErrorEvent()
  }

  override fun onEvent(event: UiEvent) {
    when (event) {
      is Cancel -> createPinRequestHandler.pinCallback?.denyAuthenticationRequest()
      is Submit -> createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(event.pin)
    }
  }

  private fun listenForPinValidationErrorEvent() {
    viewModelScope.launch {
      createPinRequestHandler.pinValidationErrorFlow.collect {
        uiState = uiState.copy(pinValidationError = it.message)
      }
    }
  }

  private fun listenForPinFinishedEvent() {
    viewModelScope.launch {
      createPinRequestHandler.finishPinCreationFlow.collect {
        _navigationEvents.send(PopBackStack)
      }
    }
  }
}
