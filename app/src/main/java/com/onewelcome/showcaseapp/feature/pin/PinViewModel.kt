package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class PinViewModel : ViewModel() {
  var uiState by mutableStateOf(State())
    protected set

  protected val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  abstract fun onEvent(event: UiEvent)

  sealed interface UiEvent {
    data object Cancel : UiEvent

    data class Submit(val pin: CharArray) : UiEvent {
      override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Submit
        return pin.contentEquals(other.pin)
      }

      override fun hashCode(): Int {
        return pin.contentHashCode()
      }
    }
  }

  data class State(
    val maxPinLength: Int = 0,
    val pinValidationError: String = "",
    val authenticationAttemptCounter: AuthenticationAttemptCounter? = null,
  )

  sealed class NavigationEvent {
    data object PopBackStack : NavigationEvent()
  }
}
