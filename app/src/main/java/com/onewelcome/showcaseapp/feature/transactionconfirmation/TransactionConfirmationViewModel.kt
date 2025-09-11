package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.NavigationEvent.NavigateToTransactionResultScreen
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.UiEvent.Accept
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.UiEvent.Reject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionConfirmationViewModel @Inject constructor(
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
  authenticateWithPushUseCase: AuthenticateWithPushUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(UiState())

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      authenticateWithPushUseCase.authenticationEvent.collect {
        uiState = uiState.copy(result = it)
        _navigationEvents.trySend(NavigateToTransactionResultScreen)
      }
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      Accept -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
      Reject -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
    }
  }

  data class UiState(
    val result: Result<CustomInfo?, OneginiMobileAuthenticationError>? = null
  )

  sealed interface UiEvent {
    data object Accept : UiEvent
    data object Reject : UiEvent
  }

  sealed interface NavigationEvent {
    data object NavigateToTransactionResultScreen : NavigationEvent
  }
}
