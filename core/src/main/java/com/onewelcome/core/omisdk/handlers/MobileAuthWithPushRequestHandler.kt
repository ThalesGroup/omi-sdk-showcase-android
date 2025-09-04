package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import javax.inject.Inject

class MobileAuthWithPushRequestHandler @Inject constructor() : OneginiMobileAuthWithPushRequestHandler {
  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiAcceptDenyCallback
  ) {
    TODO("Not yet implemented")
  }

  override fun finishAuthentication() {
    TODO("Not yet implemented")
  }

}