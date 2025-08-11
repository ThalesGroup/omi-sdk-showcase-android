package com.onewelcome.internal.testcases.logout

import com.onewelcome.core.usecase.LogoutUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class LogoutTestCases @Inject constructor(private val logoutUseCase: LogoutUseCase) {

  val tests = TestCategory(
    name = "Logout",
    testCases = listOf(
      TestCase(
        name = "logout",
        testFunction = ::logout
      )
    )
  )

  private suspend fun logout(): TestStatus {
    val result = logoutUseCase.execute()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }
}