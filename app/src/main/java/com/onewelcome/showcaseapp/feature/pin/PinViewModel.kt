package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  init {
    uiState = uiState.copy(maxPinLength = createPinRequestHandler.maxPinLength)
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.OnPinProvided -> onPinProvided(event.pin)
    }
  }

  private fun onPinProvided(pin: CharArray) {
    createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(pin)
  }
}

data class State(
  val maxPinLength: Int = 0,
  val error: String = "",
  val successfulPinCreation: Boolean = false,
)

sealed interface UiEvent {
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
