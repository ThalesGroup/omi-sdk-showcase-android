package com.onewelcome.showcaseapp.usecase

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.showcaseapp.omisdk.OmiSdkEngine
import javax.inject.Inject

class OmiSdkInitializationUseCase @Inject constructor(
  private val omiSdkEngine: OmiSdkEngine
) {

  fun initialize() {
    omiSdkEngine.omiSdk.start(object : OneginiInitializationHandler {
      override fun onSuccess(removedUserProfiles: Set<UserProfile>) {
        Log.d("InitializationHandler onSuccess", "OMI SDK successfully initialized")
      }

      override fun onError(error: OneginiInitializationError) {
        Log.d("InitializationHandler onError", error.toString())
      }
    })
  }
}
