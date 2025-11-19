package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushPinRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithPushPinRequestHandler @Inject constructor(): OneginiMobileAuthWithPushPinRequestHandler {
  var pinCallback: OneginiPinCallback? = null

  private var _authenticationAttemptCounterFlow = Channel<AuthenticationAttemptCounter>(Channel.BUFFERED)
  val authenticationAttemptCounterFlow = _authenticationAttemptCounterFlow.receiveAsFlow()

  private val _finishPinAuthenticationFlow = Channel<Unit>(Channel.BUFFERED)
  val finishPinAuthenticationFlow = _finishPinAuthenticationFlow.receiveAsFlow()

  private val _startPinAuthenticationFlow = Channel<Unit>(Channel.BUFFERED)
  val startPinAuthenticationFlow = _startPinAuthenticationFlow.receiveAsFlow()

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
    callback: OneginiPinCallback,
    attemptCounter: AuthenticationAttemptCounter,
    oneginiMobileAuthenticationError: OneginiMobileAuthenticationError?
  ) {
    pinCallback = callback
    _startPinAuthenticationFlow.trySend(Unit)
    _authenticationAttemptCounterFlow.trySend(attemptCounter)
  }

  override fun onNextAuthenticationAttempt(attemptCounter: AuthenticationAttemptCounter) {
    _authenticationAttemptCounterFlow.trySend(attemptCounter)
  }

  override fun finishAuthentication() {
    pinCallback = null
    _finishPinAuthenticationFlow.trySend(Unit)
  }
}
