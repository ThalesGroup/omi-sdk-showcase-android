package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.usecase.PinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
  private val pinUseCase: PinUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    uiState = uiState.copy(maxPinLength = pinUseCase.maxPinLength)
    listenForPinFinishedEvent()
    listenForPinValidationErrorEvent()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.OnPinProvided -> pinUseCase.onPinProvided(event.pin)
      is UiEvent.Cancel -> pinUseCase.cancel()
    }
  }

  private fun listenForPinValidationErrorEvent() {
    viewModelScope.launch {
      pinUseCase.pinValidationErrorFlow.collect {
        uiState = uiState.copy(pinValidationError = it.message)
      }
    }
  }

  private fun listenForPinFinishedEvent() {
    viewModelScope.launch {
      pinUseCase.finishPinCreationFlow.collect {
        _navigationEvents.send(NavigationEvent.PopBackStack)
      }
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
  object PopBackStack : NavigationEvent()
}
