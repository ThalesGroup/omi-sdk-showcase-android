package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomAuthRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomAuthRegistrationCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CustomAuthRegistrationAction @Inject constructor() : OneginiCustomAuthRegistrationAction {

  private val _registrationRequestFlow = Channel<RegistrationRequest>(Channel.BUFFERED)
  val registrationRequestFlow = _registrationRequestFlow.receiveAsFlow()

  var registrationCallback: OneginiCustomAuthRegistrationCallback? = null
    private set

  override fun finishRegistration(callback: OneginiCustomAuthRegistrationCallback) {
    registrationCallback = callback
    _registrationRequestFlow.trySend(RegistrationRequest(null))
  }

  fun acceptRegistration(optionalRegistrationData: String? = null) {
    registrationCallback?.acceptRegistrationRequest(optionalRegistrationData)
    registrationCallback = null
  }

  fun denyRegistration() {
    registrationCallback?.denyRegistrationRequest()
    registrationCallback = null
  }

  data class RegistrationRequest(val optionalData: String?)
}
