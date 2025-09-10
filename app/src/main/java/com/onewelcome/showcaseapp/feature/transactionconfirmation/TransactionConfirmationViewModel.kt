package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.lifecycle.ViewModel
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionConfirmationViewModel @Inject constructor(
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
) : ViewModel() {

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.Accept -> acceptTransaction()
      UiEvent.Reject -> rejectTransaction()
    }
  }

  private fun acceptTransaction() {
    mobileAuthWithPushRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
  }

  private fun rejectTransaction() {
    mobileAuthWithPushRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
  }

  sealed interface UiEvent {
    data object Accept : UiEvent
    data object Reject : UiEvent
  }
}
