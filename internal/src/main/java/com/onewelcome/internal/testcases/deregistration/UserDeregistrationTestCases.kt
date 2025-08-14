package com.onewelcome.internal.testcases.deregistration

import com.onewelcome.core.usecase.DeregisterUserUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class UserDeregistrationTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val deregisterUserUseCase: DeregisterUserUseCase
) {

  val tests = TestCategory(
    name = "User deregistration",
    testCases = listOf(
      TestCase(
        name = "deregisterUser",
        testFunction = ::deregisterUser
      )
    )
  )

  private suspend fun deregisterUser(): TestStatus {
    val userClient = getUserProfilesUseCase.execute().value.first()
    val result = deregisterUserUseCase.execute(userClient)
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }
}
