package com.onewelcome.core.usecase

import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class PinUseCase @Inject constructor(
  private val createPinRequestHandler: CreatePinRequestHandler
) {
  val pinCreationEventFlow: SharedFlow<Unit> = createPinRequestHandler.pinCreationEvents
  val maxPinLength: Int
    get() = createPinRequestHandler.maxPinLength

  fun onPinProvided(pin: CharArray) {
    createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(pin)
  }

  fun cancelPinFlow() {
    createPinRequestHandler.pinCallback?.denyAuthenticationRequest()
  }
}
