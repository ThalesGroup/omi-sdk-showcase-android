package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onegini.mobile.sdk.android.handlers.request.OneginiCreatePinRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CreatePinRequestHandler @Inject constructor() : OneginiCreatePinRequestHandler {
  private val _pinCreationFlow = Channel<Unit>(Channel.BUFFERED)
  val pinCreationFlow = _pinCreationFlow.receiveAsFlow()

  private val _finishPinCreationEvent = Channel<Unit>(Channel.BUFFERED)
  val finishPinCreationEvent = _finishPinCreationEvent.receiveAsFlow()

  private val _pinValidationErrorEvent = Channel<OneginiPinValidationError>()
  val pinValidationErrorEvent = _pinValidationErrorEvent.receiveAsFlow()

  var maxPinLength: Int = 0
  var pinCallback: OneginiPinCallback? = null

  override fun startPinCreation(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    pinLength: Int
  ) {
    pinCallback = callback
    maxPinLength = pinLength
    _pinCreationFlow.trySend(Unit)
  }

  override fun onNextPinCreationAttempt(error: OneginiPinValidationError) {
    _pinValidationErrorEvent.trySend(error)
  }

  override fun finishPinCreation() {
    pinCallback = null
    maxPinLength = 0
    _finishPinCreationEvent.trySend(Unit)
  }
}
