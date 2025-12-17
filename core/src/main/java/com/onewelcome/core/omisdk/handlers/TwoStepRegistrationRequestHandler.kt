package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomTwoStepRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwoStepRegistrationRequestHandler @Inject constructor() : OneginiCustomTwoStepRegistrationAction {
  private var registrationCallback: OneginiCustomRegistrationCallback? = null
  private val _startTwoStepInputFlow = MutableSharedFlow<TwoStepInputData>(replay = 1)
  val startTwoStepInputFlow: SharedFlow<TwoStepInputData> = _startTwoStepInputFlow.asSharedFlow()
  var optionalData: String = ""

  override fun initRegistration(
    callback: OneginiCustomRegistrationCallback, customInfo: CustomInfo?
  ) {
    // In the first step, we send initial data to the Token Server
    // This could be any data required to initialize the registration
    optionalData = if (optionalData.isBlank()) "12345" else optionalData
    callback.returnSuccess(optionalData)
  }

  override fun finishRegistration(
    callback: OneginiCustomRegistrationCallback, customInfo: CustomInfo?
  ) {
    registrationCallback = callback
    // Emit event to navigate to input screen with challenge code
    val challengeCode = optionalData
    _startTwoStepInputFlow.tryEmit(TwoStepInputData(challengeCode))
  }

  fun submitResponseCode(responseCode: String) {
    registrationCallback?.returnSuccess(responseCode)
    cleanUp()
  }

  fun cancelRegistration() {
    registrationCallback?.returnError(Exception("Registration canceled by user"))
    cleanUp()
  }

  private fun cleanUp() {
    registrationCallback = null
    _startTwoStepInputFlow.resetReplayCache()
  }

  data class TwoStepInputData(val challengeCode: String)
}
