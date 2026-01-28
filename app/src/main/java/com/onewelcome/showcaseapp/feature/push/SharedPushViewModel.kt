package com.onewelcome.showcaseapp.feature.push

import androidx.biometric.BiometricPrompt
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
import com.onewelcome.core.omisdk.handlers.MobileAuthWithBiometricRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushCustomRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushCustomRequestHandler.CustomAuthenticationData
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedPushViewModel @Inject constructor(
  private val authenticateWithPushUseCase: AuthenticateWithPushUseCase,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
  private val notificationEventDispatcher: NotificationEventDispatcher,
  private val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler,
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val preferencesManager: PreferencesManager,
  private val sdkAutoInitializationManager: SdkAutoInitializationManager,
  private val mobileAuthWithBiometricRequestHandler: MobileAuthWithBiometricRequestHandler,
  private val mobileAuthWithPushCustomRequestHandler: MobileAuthWithPushCustomRequestHandler
) : ViewModel() {
  var uiState by mutableStateOf(UiState())

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  private val _biometricEvents = Channel<BiometricEvent>(Channel.BUFFERED)
  val biometricEvents = _biometricEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      launch {
        mobileAuthWithPushRequestHandler.navigateToTransactionConfirmation.collect {
          uiState = uiState.copy(pushType = PushType.PUSH)
          _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
        }
      }
      launch {
        mobileAuthWithPushPinRequestHandler.startPinAuthenticationFlow.collect {
          uiState = uiState.copy(pushType = PushType.PUSH_PIN)
          _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
        }
      }
      launch {
        mobileAuthWithBiometricRequestHandler.startBiometricAuthenticationFlow.collect {
          uiState = uiState.copy(pushType = PushType.PUSH_BIOMETRIC, cryptoObject = it)
          _navigationEvents.trySend(NavigationEvent.NavigateToTransactionConfirmationScreen)
        }
      }
      launch {
        mobileAuthWithPushCustomRequestHandler.startCustomAuthenticationFlow.collect { customData ->
          uiState = uiState.copy(pushType = PushType.PUSH_CUSTOM, customAuthData = customData)
          _navigationEvents.trySend(NavigationEvent.NavigateToCustomAuthenticationScreen)
        }
      }
      launch {
        notificationEventDispatcher.authenticationEvent.collect {
          uiState = uiState.copy(pushType = null, result = it)
          _navigationEvents.trySend(NavigationEvent.NavigateToTransactionResultScreen)
        }
      }
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.Accept ->
        when (uiState.pushType) {
          PushType.PUSH -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
          PushType.PUSH_PIN -> _navigationEvents.trySend(NavigationEvent.NavigateToPinConfirmationScreen)
          PushType.PUSH_BIOMETRIC -> uiState.cryptoObject?.let { _biometricEvents.trySend(BiometricEvent.ShowBiometricPrompt(it)) }
          PushType.PUSH_CUSTOM -> _navigationEvents.trySend(NavigationEvent.NavigateToCustomAuthenticationScreen)
          null -> {
            //no-op
          }
        }

      is UiEvent.Reject -> when (uiState.pushType) {
        PushType.PUSH -> mobileAuthWithPushRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
        PushType.PUSH_PIN -> mobileAuthWithPushPinRequestHandler.pinCallback?.denyAuthenticationRequest()
        PushType.PUSH_BIOMETRIC -> mobileAuthWithBiometricRequestHandler.biometricCallback?.denyAuthenticationRequest()
        PushType.PUSH_CUSTOM -> mobileAuthWithPushCustomRequestHandler.customCallback?.denyAuthenticationRequest()
        null -> {
          //no-op
        }
      }

      is UiEvent.AcceptBiometric -> mobileAuthWithBiometricRequestHandler.biometricCallback?.userAuthenticatedSuccessfully()
      is UiEvent.DeclineBiometric -> handleBiometricError(event.errorCode)

      is UiEvent.SubmitCustomData -> mobileAuthWithPushCustomRequestHandler.customCallback?.acceptAuthenticationRequest()
      is UiEvent.RejectCustomAuth -> mobileAuthWithPushCustomRequestHandler.customCallback?.denyAuthenticationRequest()
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

  private fun proceedWithAuthentication(pushRequest: OneginiMobileAuthWithPushRequest) {
    authenticateWithPushUseCase.execute(pushRequest)
  }

  private fun handleBiometricError(errorCode: Int) {
    when (errorCode) {
      BiometricPrompt.ERROR_LOCKOUT,
      BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
      BiometricPrompt.ERROR_HW_UNAVAILABLE,
      BiometricPrompt.ERROR_NO_BIOMETRICS,
      BiometricPrompt.ERROR_USER_CANCELED -> {
        mobileAuthWithBiometricRequestHandler.biometricCallback?.fallbackToPin()
        _navigationEvents.trySend(NavigationEvent.NavigateToPinConfirmationScreen)
      }

      BiometricPrompt.ERROR_NEGATIVE_BUTTON,
      BiometricPrompt.ERROR_CANCELED -> {
        mobileAuthWithBiometricRequestHandler.biometricCallback?.denyAuthenticationRequest()
      }

      else -> mobileAuthWithBiometricRequestHandler.biometricCallback?.onBiometricAuthenticationError(errorCode)
    }
  }

  data class UiState(
    val pushRequest: OneginiMobileAuthWithPushRequest? = null,
    val result: Result<CustomInfo?, Throwable>? = null,
    val cryptoObject: BiometricPrompt.CryptoObject? = null,
    val pushType: PushType? = null,
    val customAuthData: CustomAuthenticationData? = null,
  )

  sealed interface UiEvent {
    data object Accept : UiEvent
    data object Reject : UiEvent
    data object AcceptBiometric : UiEvent
    data class DeclineBiometric(val errorCode: Int) : UiEvent
    data class SubmitCustomData(val data: String) : UiEvent
    data object RejectCustomAuth : UiEvent
  }

  sealed interface NavigationEvent {
    data object NavigateToTransactionResultScreen : NavigationEvent
    data object NavigateToTransactionConfirmationScreen : NavigationEvent
    data object NavigateToPinConfirmationScreen : NavigationEvent
    data object NavigateToCustomAuthenticationScreen : NavigationEvent
  }

  sealed interface BiometricEvent {
    data class ShowBiometricPrompt(val cryptoObject: BiometricPrompt.CryptoObject) : BiometricEvent
  }

  enum class PushType {
    PUSH, PUSH_PIN, PUSH_BIOMETRIC, PUSH_CUSTOM
  }
}
