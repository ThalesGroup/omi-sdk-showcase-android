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

  var pendingAuthenticationData: String? = null
    private set

  override fun finishAuthentication(
    callback: OneginiCustomAuthAuthenticationCallback,
    optionalData: String?
  ) {
    authenticationCallback = callback
    if (pendingAuthenticationData != null) {
      returnSuccess(pendingAuthenticationData)
      pendingAuthenticationData = null
    } else {
      _authenticationRequestFlow.trySend(AuthenticationRequest(optionalData))
    }
  }

  fun returnSuccess(optionalAuthenticationData: String? = null) {
    if (authenticationCallback != null) {
      authenticationCallback?.returnSuccess(optionalAuthenticationData)
      authenticationCallback = null
    } else {
      pendingAuthenticationData = optionalAuthenticationData
    }
  }

  fun returnError(exception: Exception) {
    authenticationCallback?.returnError(exception)
    authenticationCallback = null
  }

  data class AuthenticationRequest(val optionalData: String?)
}
