package com.onewelcome.internal.testcases.singlesignon

import androidx.core.net.toUri
import com.onewelcome.core.usecase.SingleSignOnUseCase
import com.onewelcome.core.util.Constants
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class SingleSignOnTestCases @Inject constructor(
  private val singleSignOnUseCase: SingleSignOnUseCase,
) {
  val tests = TestCategory(
    name = "Single sign-on",
    testCases = listOf(
      TestCase(
        name = "singleSignOn",
        testFunction = ::singleSignOn
      ),
    )
  )

  // SSO success depends on server config (URI whitelisted) and authenticated user,
  // not the OS. Any callback response (Ok or Err) means the SDK's SSO mechanism
  // is working at the OS level. Only an uncaught exception indicates an OS issue.
  private suspend fun singleSignOn(): TestStatus {
    return try {
      singleSignOnUseCase.execute(Constants.SSO_URL.toUri())
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }
}
