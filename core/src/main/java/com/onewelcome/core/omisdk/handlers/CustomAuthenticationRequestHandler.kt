package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiCustomAuthenticationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomAuthenticationRequestHandler @Inject constructor() : OneginiCustomAuthenticationRequestHandler {

  private val _startAuthenticationFlow = MutableSharedFlow<CustomAuthRequest>(0, extraBufferCapacity = 1)
  val startAuthenticationFlow = _startAuthenticationFlow

  private val _finishAuthenticationFlow = Channel<Unit>(Channel.BUFFERED)

  var customCallback: OneginiCustomCallback? = null
    private set

  var currentUserProfile: UserProfile? = null
    private set

  override fun startAuthentication(userProfile: UserProfile, callback: OneginiCustomCallback) {
    customCallback = callback
    currentUserProfile = userProfile
    _startAuthenticationFlow.tryEmit(CustomAuthRequest(userProfile))
  }

  override fun finishAuthentication() {
    customCallback = null
    currentUserProfile = null
    _finishAuthenticationFlow.trySend(Unit)
  }


  fun acceptAuthenticationRequest() {
    customCallback?.acceptAuthenticationRequest()
  }

  data class CustomAuthRequest(val userProfile: UserProfile)
}
