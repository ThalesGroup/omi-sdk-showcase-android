package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomAuthAuthenticationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomAuthAuthenticationCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomAuthAuthenticationAction @Inject constructor() : OneginiCustomAuthAuthenticationAction {

  private val _authenticationRequestFlow = Channel<AuthenticationRequest>(Channel.BUFFERED)
  val authenticationRequestFlow = _authenticationRequestFlow.receiveAsFlow()

  var authenticationCallback: OneginiCustomAuthAuthenticationCallback? = null
    private set

  override fun finishAuthentication(
    callback: OneginiCustomAuthAuthenticationCallback,
    optionalData: String?
  ) {
    authenticationCallback = callback
    _authenticationRequestFlow.trySend(AuthenticationRequest(optionalData))
  }

  fun returnSuccess(optionalAuthenticationData: String? = null) {
    authenticationCallback?.returnSuccess(optionalAuthenticationData)
    authenticationCallback = null
  }

  fun returnError(exception: Exception) {
    authenticationCallback?.returnError(exception)
    authenticationCallback = null
  }

  data class AuthenticationRequest(val optionalData: String?)
}
