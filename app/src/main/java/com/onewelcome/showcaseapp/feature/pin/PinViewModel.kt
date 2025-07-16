package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {

    uiState = uiState.copy(maxPinLength = createPinRequestHandler.maxPinLength)
    listenForPinFinishedEvent()
    listenForPinValidationErrorEvent()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.OnPinProvided -> createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(event.pin)
      is UiEvent.Cancel -> createPinRequestHandler.pinCallback?.denyAuthenticationRequest()
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
        _navigationEvents.send(NavigationEvent.PopBackStack)
      }
    }
  }

  data class State(
    val maxPinLength: Int = 0,
    val pinValidationError: String = "",
  )

  sealed interface UiEvent {
    data object Cancel : UiEvent
    data class OnPinProvided(val pin: CharArray) : UiEvent {
      override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OnPinProvided
        return pin.contentEquals(other.pin)
      }

      override fun hashCode(): Int {
        return pin.contentHashCode()
      }
    }
  }

  sealed class NavigationEvent {
    data object PopBackStack : NavigationEvent()
  }
}

