package com.onewelcome.core.omisdk.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.onegini.mobile.sdk.android.handlers.request.OneginiBrowserRegistrationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBrowserRegistrationCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserRegistrationRequestHandler @Inject constructor(
  @ApplicationContext private val context: Context,
) : OneginiBrowserRegistrationRequestHandler {
  private var browserRegistrationCallback: OneginiBrowserRegistrationCallback? = null
  override fun startRegistration(
    url: Uri,
    registrationCallback: OneginiBrowserRegistrationCallback
  ) {
    browserRegistrationCallback = registrationCallback
    Intent(Intent.ACTION_VIEW, url)
      .apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
      }
      .let { context.startActivity(it) }
  }

  fun cancelRegistration() {
    browserRegistrationCallback?.denyRegistration()
  }

  fun handleRegistrationCallback(uri: Uri) {
    browserRegistrationCallback?.handleRegistrationCallback(uri)
  }
}
