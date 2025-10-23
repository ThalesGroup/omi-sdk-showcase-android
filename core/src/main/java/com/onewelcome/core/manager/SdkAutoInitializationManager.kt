package com.onewelcome.core.manager

import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdkAutoInitializationManager @Inject constructor(
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase,
  private val dataStore: ShowcaseDataStore
) {
  var deferredResult: Deferred<Result<Set<UserProfile>, OneginiInitializationError>>? = null
  suspend fun execute() {
    if (dataStore.isSdkAutoInitializationEnabled().first()) {
      deferredResult = CoroutineScope(Dispatchers.IO).async {
        val sdkInitializationSettings = OmiSdkInitializationSettings(
          shouldStoreCookies = true,
          httpReadTimeout = null,
          deviceConfigCacheDuration = null,
          httpConnectTimeout = null,
          handlers = HandlerType.entries,
        )
        delay(4000)
        sdkInitializationUseCase.initialize(sdkInitializationSettings)
      }
    }
  }
}