package com.onewelcome.internal.testcases.browserregistation

import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.core.util.TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS
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

  private suspend fun getBrowserIdentityProviders(): TestStatus {
    sdkInitializationUseCase.initialize(TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  private suspend fun sdkNotInitializedGetBrowserIdentityProviders(): TestStatus {
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isErr) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  private fun isRegistrationInProgress(): TestStatus {
    val result = browserRegistrationUseCase.isRegistrationInProgress()
    return if (result) {
      TestStatus.Failed
    } else {
      TestStatus.Passed
    }
  }
}
