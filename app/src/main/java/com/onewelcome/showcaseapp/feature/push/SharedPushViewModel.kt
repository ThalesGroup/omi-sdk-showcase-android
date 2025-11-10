package com.onewelcome.showcaseapp.feature.push

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.manager.PreferencesManager
import com.onewelcome.core.manager.SdkAutoInitializationManager
import com.onewelcome.core.notification.NotificationEventDispatcher
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedPushViewModel @Inject constructor(
  private val authenticateWithPushUseCase: AuthenticateWithPushUseCase,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
  private val notificationEventDispatcher: NotificationEventDispatcher,
  private val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler,
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val preferencesManager: PreferencesManager,
  private val sdkAutoInitializationManager: SdkAutoInitializationManager
) : ViewModel() {
  var uiState by mutableStateOf(UiState())

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      notificationEventDispatcher.authenticationEvent.collect {
        uiState = uiState.copy(result = it)
        _navigationEvents.trySend(NavigationEvent.NavigateToTransactionResultScreen)
      }
      mobileAuthWithPushPinRequestHandler.startPinAuthenticationFlow.collect {
        _navigationEvents.trySend(NavigationEvent.NavigateToPinConfirmationScreen)
      }
    }
  }

  fun onNewPush(pushRequest: OneginiMobileAuthWithPushRequest) {
    viewModelScope.launch {
      uiState = uiState.copy(pushRequest = pushRequest)
      handleAuthentication(pushRequest)
    }
  }

  private suspend fun handleAuthentication(pushRequest: OneginiMobileAuthWithPushRequest) {
    if (isSdkInitializedUseCase.execute()) {
      proceedWithAuthentication(pushRequest)
    } else {
      handleSdkNotInitialized(pushRequest)
    }
  }

  private suspend fun handleSdkNotInitialized(pushRequest: OneginiMobileAuthWithPushRequest) {
    if (preferencesManager.isSdkAutoInitializationEnabled()) {
      handleSdkAutoInitialize(pushRequest)
    } else {
      handleSdkNotInitialized(IllegalStateException("SDK needs to be initialized to handle push transactions"))
    }
  }

  private suspend fun handleSdkAutoInitialize(pushRequest: OneginiMobileAuthWithPushRequest) {
    sdkAutoInitializationManager.deferredResult?.await()
      ?.onSuccess { proceedWithAuthentication(pushRequest) }
      ?.onFailure { handleSdkNotInitialized(it) }
  }

  private fun handleSdkNotInitialized(error: Throwable) {
    uiState = uiState.copy(result = Err(error))
    _navigationEvents.trySend(NavigationEvent.NavigateToTransactionResultScreen)
  }

  private suspend fun proceedWithAuthentication(pushRequest: OneginiMobileAuthWithPushRequest) {
    authenticateWithPushUseCase.execute(pushRequest)
    _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
    _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
    withContext(Dispatchers.IO) {
      authenticateWithPushUseCase.execute(pushRequest)
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.Accept -> {
        mobileAuthWithPushRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
        mobileAuthWithPushPinRequestHandler.pinCallback
      }

      UiEvent.Reject -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
    }
  }

  data class UiState(
    val pushRequest: OneginiMobileAuthWithPushRequest? = null,
    val result: Result<CustomInfo?, Throwable>? = null,
  )

  sealed interface UiEvent {
    data object Accept : UiEvent
    data object Reject : UiEvent
  }

  sealed interface NavigationEvent {
    data object NavigateToTransactionResultScreen : NavigationEvent
    data object NavigateToTransactionConfirmationScreen : NavigationEvent
    data object NavigateToPinConfirmationScreen : NavigationEvent
  }
}

