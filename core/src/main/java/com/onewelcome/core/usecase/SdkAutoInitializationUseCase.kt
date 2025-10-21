package com.onewelcome.core.usecase

import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdkAutoInitializationUseCase @Inject constructor(
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase,
  private val dataStore: ShowcaseDataStore
) {
  suspend fun execute() {
    if (dataStore.isSdkAutoInitializationEnabled().first()) {
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
