package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushCustomRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithPushCustomRequestHandler @Inject constructor() : OneginiMobileAuthWithPushCustomRequestHandler {

  var customCallback: OneginiCustomCallback? = null
    private set

  var currentRequest: OneginiMobileAuthenticationRequest? = null
    private set

  private val _startCustomAuthenticationFlow = Channel<OneginiMobileAuthenticationRequest>(Channel.BUFFERED)
  val startCustomAuthenticationFlow = _startCustomAuthenticationFlow.receiveAsFlow()

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiCustomCallback
  ) {
    customCallback = callback
    currentRequest = mobileAuthenticationRequest
    _startCustomAuthenticationFlow.trySend(mobileAuthenticationRequest)
  }

  override fun finishAuthentication() {
    customCallback = null
    currentRequest = null
  }

  fun acceptAuthenticationRequest() {
    customCallback?.acceptAuthenticationRequest()
  }

  fun denyAuthenticationRequest() {
    customCallback?.denyAuthenticationRequest()
  }

  fun fallbackToPin() {
    customCallback?.fallbackToPin()
  }
}
