package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onegini.mobile.sdk.android.handlers.request.OneginiCreatePinRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatePinRequestHandler @Inject constructor() : OneginiCreatePinRequestHandler {
  var pinCallback: OneginiPinCallback? = null
  private val _pinCreationEvents = MutableSharedFlow<Unit>(replay = 1)
  val pinCreationEvents: SharedFlow<Unit> = _pinCreationEvents

  override fun startPinCreation(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    pinLength: Int
  ) {
    pinCallback = callback
    _pinCreationEvents.tryEmit(Unit)
  }

  //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
  override fun onNextPinCreationAttempt(error: OneginiPinValidationError) {
    Log.d("PinRequestHandler onNextPinCreationAttempt", error.toString())
  }

  //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
  override fun finishPinCreation() {
    Log.d("PinRequestHandler finishPinCreation", "")
  }
}
