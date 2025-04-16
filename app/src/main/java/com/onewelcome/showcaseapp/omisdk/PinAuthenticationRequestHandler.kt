package com.onewelcome.showcaseapp.omisdk

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.OneginiPinAuthenticationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import javax.inject.Inject

class PinAuthenticationRequestHandler @Inject constructor() : OneginiPinAuthenticationRequestHandler {
  override fun startAuthentication(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    attemptCounter: AuthenticationAttemptCounter
  ) {
    //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
    callback.acceptAuthenticationRequest(charArrayOf('6', '6', '6', '7', '7'))
  }

  //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
  override fun onNextAuthenticationAttempt(attemptCounter: AuthenticationAttemptCounter) {
    Log.d("PinAuthenticationRequestHandler onNextAuthenticationAttempt", attemptCounter.toString())
  }

  //TODO: https://onewelcome.atlassian.net/browse/EXAMPLEAND-163
  override fun finishAuthentication() {
    Log.d("PinAuthenticationRequestHandler finishAuthentication", "")
  }
}
