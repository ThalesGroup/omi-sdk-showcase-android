package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithOtpRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithOtpRequestHandler @Inject constructor() : OneginiMobileAuthWithOtpRequestHandler {

  var acceptDenyCallback: OneginiAcceptDenyCallback? = null

  private val _startAuthWithOtpFlow = Channel<OneginiMobileAuthenticationRequest>(Channel.BUFFERED)
  val startAuthWithOtpFlow = _startAuthWithOtpFlow.receiveAsFlow()

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiAcceptDenyCallback
  ) {
    acceptDenyCallback = callback
    _startAuthWithOtpFlow.trySend(mobileAuthenticationRequest)
  }

  override fun finishAuthentication() {
    acceptDenyCallback = null
  }
}
