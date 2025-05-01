package com.onewelcome.core.omisdk

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onegini.mobile.sdk.android.handlers.request.OneginiCreatePinRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import javax.inject.Inject

class CreatePinRequestHandler @Inject constructor() : OneginiCreatePinRequestHandler {
  override fun startPinCreation(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    pinLength: Int
  ) {
    //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
    callback.acceptAuthenticationRequest(charArrayOf('6', '6', '6', '7', '7'))
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
