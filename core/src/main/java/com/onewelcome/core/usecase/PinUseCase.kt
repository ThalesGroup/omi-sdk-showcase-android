package com.onewelcome.core.usecase

import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PinUseCase @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler
) {
  val startPinCreationFlow: Flow<Unit> = createPinRequestHandler.startPinCreationFlow
  val finishPinCreationFlow: Flow<Unit> = createPinRequestHandler.finishPinCreationFlow
  val pinValidationErrorFlow: Flow<OneginiPinValidationError> = createPinRequestHandler.pinValidationErrorFlow
  val maxPinLength: Int
    get() = createPinRequestHandler.maxPinLength

  fun onPinProvided(pin: CharArray) {
    createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(pin)
  }

  fun cancel() {
    createPinRequestHandler.pinCallback?.denyAuthenticationRequest()
  }
}
