package com.onewelcome.core.usecase

import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PinUseCase @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler
) {
  val pinCreationEventFlow: Flow<Unit> = createPinRequestHandler.pinCreationFlow
  val pinFinishedEventFlow: Flow<Unit> = createPinRequestHandler.finishPinCreationEvent
  val pinValidationErrorEvent: Flow<OneginiPinValidationError> = createPinRequestHandler.pinValidationErrorEvent
  val maxPinLength: Int
    get() = createPinRequestHandler.maxPinLength

  fun onPinProvided(pin: CharArray) {
    createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(pin)
  }

  fun cancelPinFlow() {
    createPinRequestHandler.pinCallback?.denyAuthenticationRequest()
  }
}
