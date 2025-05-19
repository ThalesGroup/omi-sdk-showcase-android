package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onewelcome.core.usecase.PinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
  private val pinUseCase: PinUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  init {
    uiState = uiState.copy(maxPinLength = pinUseCase.maxPinLength)
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.OnPinProvided -> pinUseCase.onPinProvided(event.pin)
      UiEvent.CancelPinFlow -> pinUseCase.cancelPinFlow()
    }
  }

}

data class State(
  val maxPinLength: Int = 0,
  val error: String = "",
  val successfulPinCreation: Boolean = false,
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
