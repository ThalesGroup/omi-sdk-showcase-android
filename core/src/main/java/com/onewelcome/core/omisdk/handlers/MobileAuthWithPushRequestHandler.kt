package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithPushRequestHandler @Inject constructor() : OneginiMobileAuthWithPushRequestHandler {

  var acceptDenyCallback: OneginiAcceptDenyCallback? = null

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiAcceptDenyCallback
  ) {
    acceptDenyCallback = callback
  }

  override fun finishAuthentication() {
    acceptDenyCallback = null
  }
}
