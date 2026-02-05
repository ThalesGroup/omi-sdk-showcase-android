package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomAuthDeregistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomAuthDeregistrationCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CustomAuthDeregistrationAction @Inject constructor() : OneginiCustomAuthDeregistrationAction {

  private val _deregistrationRequestFlow = Channel<DeregistrationRequest>(Channel.BUFFERED)
  val deregistrationRequestFlow = _deregistrationRequestFlow.receiveAsFlow()

  var deregistrationCallback: OneginiCustomAuthDeregistrationCallback? = null
    private set

  override fun finishDeregistration(
    deregistrationCallback: OneginiCustomAuthDeregistrationCallback,
    optionalData: String?
  ) {
    this.deregistrationCallback = deregistrationCallback
    _deregistrationRequestFlow.trySend(DeregistrationRequest(optionalData))
  }

  fun acceptDeregistration(optionalDeregistrationData: String? = null) {
    deregistrationCallback?.acceptDeregistrationRequest(optionalDeregistrationData)
    deregistrationCallback = null
  }


  data class DeregistrationRequest(
    val optionalData: String?,
    val timestamp: Long = System.currentTimeMillis()
  )
}
