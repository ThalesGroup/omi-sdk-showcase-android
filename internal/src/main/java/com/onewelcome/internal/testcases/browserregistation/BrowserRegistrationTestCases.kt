package com.onewelcome.internal.testcases.browserregistation

import com.onewelcome.core.usecase.GetBrowserIdentityProvidersUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class BrowserRegistrationTestCases @Inject constructor(
  private val getBrowserIdentityProvidersUseCase: GetBrowserIdentityProvidersUseCase,
) {
  val tests = TestCategory(
    name = "Browser registration",
    testCases = listOf(
      TestCase(
        name = "getBrowserIdentityProviders",
        testFunction = ::getBrowserIdentityProviders
      ),
    )
  )

  private suspend fun getBrowserIdentityProviders(): TestStatus {
    val result = getBrowserIdentityProvidersUseCase.execute()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }
}
