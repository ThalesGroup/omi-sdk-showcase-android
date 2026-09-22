package com.onewelcome.showcaseapp.feature.qrcodescanning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.omisdk.actions.QrCodeRegistrationAction
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the QR Code Scanner screen.
 *
 * Handles OTP submission and cancellation for the QR code scanning flow.
 * Note: [com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel]
 * serves the same purpose for the One-Step Registration OTP entry screen.
 * Both are intentionally kept as separate screen-scoped ViewModels to maintain
 * independent lifecycle and navigation contexts.
 */
@HiltViewModel
class QrCodeScanningViewModel @Inject constructor(
  private val qrCodeRegistrationAction: QrCodeRegistrationAction
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.UpdateOtpValue -> uiState = uiState.copy(otp = event.otp)
      is UiEvent.ProceedWithRegistration -> proceedWithRegistration()
      is UiEvent.CancelRegistration -> cancelRegistration()
    }
  }

  private fun proceedWithRegistration() {
    viewModelScope.launch {
      qrCodeRegistrationAction.getCallback()?.returnSuccess(uiState.otp)
      _navigationEvents.send(NavigationEvent.NavigateBack)
    }
  }

  private fun cancelRegistration() {
    viewModelScope.launch {
      qrCodeRegistrationAction.getCallback()?.returnError(Exception("Registration cancelled by user"))
      _navigationEvents.send(NavigationEvent.NavigateBack)
    }
  }

  data class State(
    val otp: String = ""
  )

  sealed interface UiEvent {
    data class UpdateOtpValue(val otp: String) : UiEvent
    data object ProceedWithRegistration : UiEvent
    data object CancelRegistration : UiEvent
  }

  sealed interface NavigationEvent {
    data object NavigateBack : NavigationEvent
  }
}
