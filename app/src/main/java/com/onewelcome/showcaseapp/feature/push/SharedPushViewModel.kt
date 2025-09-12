package com.onewelcome.showcaseapp.feature.push

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedPushViewModel @Inject constructor(
  private val authenticateWithPushUseCase: AuthenticateWithPushUseCase,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
) : ViewModel() {
  var uiState by mutableStateOf(UiState())

  private val _navigationEvents = Channel<NavigationEvent>(Channel.Factory.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      authenticateWithPushUseCase.authenticationEvent.collect {
        uiState = uiState.copy(result = it)
        _navigationEvents.trySend(NavigationEvent.NavigateToTransactionResultScreen)
      }
    }
  }

  fun onNewPush(pushRequest: OneginiMobileAuthWithPushRequest) {
    viewModelScope.launch {
      uiState = uiState.copy(pushRequest = pushRequest)
      authenticateWithPushUseCase.execute(pushRequest)
      _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.Accept -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
      UiEvent.Reject -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
    }
  }

  data class UiState(
    val pushRequest: OneginiMobileAuthWithPushRequest? = null,
    val result: Result<CustomInfo?, OneginiMobileAuthenticationError>? = null
  )

  sealed interface UiEvent {
    data object Accept : UiEvent
    data object Reject : UiEvent
  }

  sealed interface NavigationEvent {
    data object NavigateToTransactionResultScreen : NavigationEvent
    data object NavigateToTransactionConfirmationScreen : NavigationEvent
  }
}
