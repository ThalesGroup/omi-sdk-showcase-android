package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onegini.mobile.sdk.android.handlers.request.OneginiPinAuthenticationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinAuthenticationRequestHandler @Inject constructor() : OneginiPinAuthenticationRequestHandler {
  private val _startPinAuthenticationFlow = Channel<Unit>(Channel.BUFFERED)
  val startPinAuthenticationFlow = _startPinAuthenticationFlow.receiveAsFlow()

  private val _finishPinAuthenticationFlow = Channel<Unit>(Channel.BUFFERED)
  val finishPinAuthenticationFlow = _finishPinAuthenticationFlow.receiveAsFlow()

  private val _authenticationAttemptCounterFlow = Channel<AuthenticationAttemptCounter>()
  val authenticationAttemptCounterFlow = _authenticationAttemptCounterFlow.receiveAsFlow()
  
  var pinCallback: OneginiPinCallback? = null

  override fun startAuthentication(
    userProfile: UserProfile,
    callback: OneginiPinCallback,
    attemptCounter: AuthenticationAttemptCounter
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
