package com.onewelcome.internal.testcases.browserregistation

import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class BrowserRegistrationTestCases @Inject constructor(
  private val browserRegistrationUseCase: BrowserRegistrationUseCase,
) {
  val tests = TestCategory(
    name = "Browser registration",
    testCases = listOf(
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
    val result = browserRegistrationUseCase.getBrowserIdentityProviders()
    return if (result.isOk) {
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
