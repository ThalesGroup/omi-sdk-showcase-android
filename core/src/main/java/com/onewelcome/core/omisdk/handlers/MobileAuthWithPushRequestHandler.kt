package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithPushRequestHandler @Inject constructor() : OneginiMobileAuthWithPushRequestHandler {

  var acceptDenyCallback: OneginiAcceptDenyCallback? = null

  private val _navigateToTransactionConfirmation = Channel<Unit>(Channel.BUFFERED)
  val navigateToTransactionConfirmation = _navigateToTransactionConfirmation.receiveAsFlow()

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiAcceptDenyCallback
  ) {
    acceptDenyCallback = callback
    _navigateToTransactionConfirmation.trySend(Unit)
  }

  override fun finishAuthentication() {
    acceptDenyCallback = null
  }
}
