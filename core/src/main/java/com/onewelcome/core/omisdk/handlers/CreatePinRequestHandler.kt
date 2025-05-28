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
class CreatePinRequestHandler @Inject constructor() : OneginiCreatePinRequestHandler {
  private val _startPinCreationFlow = Channel<Unit>(Channel.BUFFERED)
  val startPinCreationFlow = _startPinCreationFlow.receiveAsFlow()

  private val _finishPinCreationFlow = Channel<Unit>(Channel.BUFFERED)
  val finishPinCreationFlow = _finishPinCreationFlow.receiveAsFlow()

  private val _pinValidationErrorFlow = Channel<OneginiPinValidationError>()
  val pinValidationErrorFlow = _pinValidationErrorFlow.receiveAsFlow()

  var maxPinLength: Int = 0
  var pinCallback: OneginiPinCallback? = null

  override fun startPinCreation(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    pinLength: Int
  ) {
    pinCallback = callback
    maxPinLength = pinLength
    _startPinCreationFlow.trySend(Unit)
  }

  override fun onNextPinCreationAttempt(error: OneginiPinValidationError) {
    _pinValidationErrorFlow.trySend(error)
  }

  override fun finishPinCreation() {
    pinCallback = null
    maxPinLength = 0
    _finishPinCreationFlow.trySend(Unit)
  }
}
