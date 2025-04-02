package com.onewelcome.showcaseapp.omisdk

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import javax.inject.Inject

class OmiSdkInitializationHandler @Inject constructor() : OneginiInitializationHandler {
  override fun onSuccess(removedUserProfiles: Set<UserProfile>) {
    Log.d("InitializationHandler onSuccess", "OMI SDK successfully initialized")
  }

  override fun onError(error: OneginiInitializationError) {
    Log.d("InitializationHandler onError", error.toString())
  }
}
