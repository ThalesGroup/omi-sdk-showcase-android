package com.onewelcome.core.omisdk.actions

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Inject

class QrCodeRegistrationAction @Inject constructor() : OneginiCustomRegistrationAction {

  var customRegistrationCallback: Deferred<OneginiCustomRegistrationCallback>? = null

  override fun finishRegistration(
    oneginiCustomRegistrationCallback: OneginiCustomRegistrationCallback,
    customInfo: CustomInfo?
  ) {
    customRegistrationCallback = CoroutineScope(Dispatchers.IO).async {
      oneginiCustomRegistrationCallback
    }
  }
}
