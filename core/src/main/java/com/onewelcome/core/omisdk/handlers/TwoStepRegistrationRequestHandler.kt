package com.onewelcome.core.omisdk.handlers

import android.util.Log
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
  private var oneginiCustomRegistrationCallback: OneginiCustomRegistrationCallback? = null
    private var initCallback: OneginiCustomRegistrationCallback? = null

  private val _startTwoStepInputFlow = MutableSharedFlow<TwoStepInputData>(replay = 1)
  val startTwoStepInputFlow: SharedFlow<TwoStepInputData> = _startTwoStepInputFlow.asSharedFlow()

     private var optionalData: String = ""

    public fun setOptionalData(data : String){
        optionalData = data
    }

    override fun initRegistration(
      registrationCallback: OneginiCustomRegistrationCallback, customInfo: CustomInfo?
  ) {
    // In the first step, we send initial data to the Token Server
      initCallback = registrationCallback
      if(optionalData.isNullOrBlank()){
          registrationCallback.returnSuccess("12345")
      }else{
          registrationCallback.returnSuccess(optionalData)
      }
  }

  override fun finishRegistration(
    callback: OneginiCustomRegistrationCallback, customInfo: CustomInfo?
  ) {
    oneginiCustomRegistrationCallback = callback
    _startTwoStepInputFlow.tryEmit(TwoStepInputData(optionalData?.takeIf { it.isNotEmpty() } ?: "12345"))
  }

  fun submitResponseCode(responseCode: String) {
    oneginiCustomRegistrationCallback?.returnSuccess(responseCode)
    cleanUp()
  }

  fun cancelRegistration() {
    oneginiCustomRegistrationCallback?.returnError(Exception("Registration canceled by user"))
    cleanUp()
  }

  private fun cleanUp() {
    oneginiCustomRegistrationCallback = null
    _startTwoStepInputFlow.resetReplayCache()
  }
    fun isTwoStepregistrationInProgress(): Boolean = oneginiCustomRegistrationCallback != null
  data class TwoStepInputData(val challengeCode: String)
}
