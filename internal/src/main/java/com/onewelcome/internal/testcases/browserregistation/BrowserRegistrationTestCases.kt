package com.onewelcome.internal.testcases.browserregistation

import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class BrowserRegistrationTestCases @Inject constructor(
  private val browserRegistrationUseCase: BrowserRegistrationUseCase,
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase
) {
  suspend fun getBrowserIdentityProviders(): TestStatus {
    sdkInitializationUseCase.initialize(DEFAULT_SETTINGS)
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  suspend fun sdkNotInitializedGetBrowserIdentityProviders(): TestStatus {
    sdkInitializationUseCase.initialize(DEFAULT_SETTINGS)
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  companion object {
    private val DEFAULT_SETTINGS = OmiSdkInitializationSettings(
      shouldStoreCookies = true,
      httpConnectTimeout = null,
      httpReadTimeout = null,
      deviceConfigCacheDuration = null
    )
  }
}
