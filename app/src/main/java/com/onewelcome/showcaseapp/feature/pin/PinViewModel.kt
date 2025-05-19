package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.usecase.PinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
  private val pinUseCase: PinUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  init {
    uiState = uiState.copy(maxPinLength = pinUseCase.maxPinLength)
    listenForPinFinishedEvent()
    listenForPinValidationErrorEvent()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.OnPinProvided -> pinUseCase.onPinProvided(event.pin)
      is UiEvent.CancelPinFlow -> pinUseCase.cancelPinFlow()
    }
  }

  private fun listenForPinValidationErrorEvent() {
    viewModelScope.launch {
      pinUseCase.pinValidationErrorEvent.collect {
        uiState = uiState.copy(pinValidationError = it.message)
      }
    }
  }

  private fun listenForPinFinishedEvent() {
    viewModelScope.launch {
      pinUseCase.pinFinishedEventFlow.collect {
        uiState = uiState.copy(finished = true)
      }
    }
  }
}

data class State(
  val maxPinLength: Int = 0,
  val finished: Boolean = false,
  val pinValidationError: String = "",
)

sealed interface UiEvent {
  data object CancelPinFlow : UiEvent
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
