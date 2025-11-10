package com.onewelcome.showcaseapp.feature.changepin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.ChangePinUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePinViewModel @Inject constructor(
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val changePinUseCase: ChangePinUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val createPinRequestHandler: CreatePinRequestHandler,
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    uiState = uiState.copy(isSdkInitialized = isSdkInitialized)
    if (isSdkInitialized) {
      updateAuthenticatedUserProfile()
    }
    listenForPinInputScreenNavigationEvents()
  }

  private fun updateAuthenticatedUserProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfile = it) }
      .onFailure { uiState = uiState.copy(authenticatedUserProfile = null) }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartPinChange -> {
        changePin()
      }
    }
  }

  private fun listenForPinInputScreenNavigationEvents() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.startPinAuthenticationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinAuthenticationScreen)
      }
    }
    viewModelScope.launch {
      createPinRequestHandler.startPinCreationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinCreationScreen)
      }
    }
  }

  private fun changePin() {
    viewModelScope.launch {
      uiState = uiState.copy(result = changePinUseCase.execute())
    }
  }

  data class State(
    val result: Result<Unit, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
  )

  sealed interface UiEvent {
    data object StartPinChange : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinAuthenticationScreen : NavigationEvent
    data object ToPinCreationScreen : NavigationEvent
  }
}
