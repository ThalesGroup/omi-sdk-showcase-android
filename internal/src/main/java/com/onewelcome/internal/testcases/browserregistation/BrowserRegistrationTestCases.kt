package com.onewelcome.internal.testcases.browserregistation

import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class BrowserRegistrationTestCases @Inject constructor(
  private val browserRegistrationUseCase: BrowserRegistrationUseCase,
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase
) {
  val tests = TestCategory(
    name = "Browser registration",
    testCases = listOf(
      TestCase(
        name = "sdkNotInitializedGetBrowserIdentityProviders",
        testFunction = ::sdkNotInitializedGetBrowserIdentityProviders
      ),
      TestCase(
        name = "getBrowserIdentityProviders",
        testFunction = ::getBrowserIdentityProviders
      ),
      TestCase(
        name = "isRegistrationInProgress",
        testFunction = ::isRegistrationInProgress
      ),
    )
  )

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
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isErr) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  fun isRegistrationInProgress(): TestStatus {
    val result = browserRegistrationUseCase.isRegistrationInProgress()
    return if (result) {
      TestStatus.Failed
    } else {
      TestStatus.Passed
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
