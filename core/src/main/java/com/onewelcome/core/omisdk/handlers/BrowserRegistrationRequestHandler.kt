package com.onewelcome.core.omisdk.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.OneginiBrowserRegistrationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBrowserRegistrationCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BrowserRegistrationRequestHandler @Inject constructor(
  @ApplicationContext private val context: Context,
) : OneginiBrowserRegistrationRequestHandler {
  private var browserRegistrationCallback: OneginiBrowserRegistrationCallback? = null
  override fun startRegistration(
    url: Uri,
    registrationCallback: OneginiBrowserRegistrationCallback
  ) {
    Log.d("BrowserRegistrationRequestHandler", url.toString())
    browserRegistrationCallback = registrationCallback
    val intent = Intent(Intent.ACTION_VIEW, url);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

    context.startActivity(intent)
    registrationCallback.handleRegistrationCallback(url)
  }

  fun handleRegistrationCallback(uri: Uri) {
    browserRegistrationCallback?.handleRegistrationCallback(uri)
  }
}
