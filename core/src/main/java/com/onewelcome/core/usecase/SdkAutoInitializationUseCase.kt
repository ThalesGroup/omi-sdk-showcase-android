package com.onewelcome.core.usecase

import android.util.Log
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdkAutoInitializationUseCase @Inject constructor(
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase
) {
  var deferredResult: Deferred<Result<Set<UserProfile>, OneginiInitializationError>>? = null
  fun execute() {
    deferredResult = CoroutineScope(Dispatchers.IO).async {
      // bez deleya z auto - ok
      // z delayem z auto - ok ale nie ma spinnera
      // bez sdk auto - ok
      // bez internetu - ok
//      delay(5000)
      val sdkInitializationSettings = OmiSdkInitializationSettings(
        shouldStoreCookies = true,
        httpReadTimeout = null,
        deviceConfigCacheDuration = null,
        httpConnectTimeout = null,
        handlers = HandlerType.entries,
      )
      sdkInitializationUseCase.initialize(sdkInitializationSettings)
    }
  }
}
