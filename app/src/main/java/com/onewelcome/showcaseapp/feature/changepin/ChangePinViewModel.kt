package com.onewelcome.showcaseapp.feature.changepin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class ChangePinViewModel @Inject constructor() : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.CancelPinChange -> TODO()
      is UiEvent.StartPinChange -> TODO()
    }
  }

  data class State(
    val result: Result<Unit, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
  )

  sealed interface UiEvent {
    data object StartPinChange : UiEvent
    data object CancelPinChange : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
  }
}
