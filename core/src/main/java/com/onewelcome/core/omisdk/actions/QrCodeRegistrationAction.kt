package com.onewelcome.core.omisdk.actions

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrCodeRegistrationAction @Inject constructor() : OneginiCustomRegistrationAction {

  private var _channelCallback = Channel<OneginiCustomRegistrationCallback>(Channel.BUFFERED)
  private var customRegistrationCallback: OneginiCustomRegistrationCallback? = null

  fun reset() {
    _channelCallback = Channel(Channel.BUFFERED)
    customRegistrationCallback = null
  }

  fun getCallback(): OneginiCustomRegistrationCallback? = customRegistrationCallback

  override fun finishRegistration(
    oneginiCustomRegistrationCallback: OneginiCustomRegistrationCallback,
    customInfo: CustomInfo?
  ) {
    customRegistrationCallback = oneginiCustomRegistrationCallback
    _channelCallback.trySend(oneginiCustomRegistrationCallback)
  }

  suspend fun awaitCallback(): OneginiCustomRegistrationCallback {
    return _channelCallback.receive()
  }
}
