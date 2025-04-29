package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.OneginiCustomAuthenticationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import javax.inject.Inject

class BrowserCustomAuthenticationRequestHandler @Inject constructor() : OneginiCustomAuthenticationRequestHandler {
  override fun startAuthentication(
    userProfile: UserProfile,
    callback: OneginiCustomCallback
  ) {
    Log.d("startAuthentication", userProfile.toString())
  }

  override fun finishAuthentication() {
    Log.d("finishAuthentication", "")
  }
}
