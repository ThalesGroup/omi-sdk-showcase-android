package com.onewelcome.core.omisdk.handlers

import android.net.Uri
import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.OneginiBrowserRegistrationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBrowserRegistrationCallback
import javax.inject.Inject

class BrowserRegistrationRequestHandler @Inject constructor() : OneginiBrowserRegistrationRequestHandler {
  override fun startRegistration(
    url: Uri,
    registrationCallback: OneginiBrowserRegistrationCallback
  ) {
    Log.d("BrowserRegistrationRequestHandler", url.toString())
  }
}
