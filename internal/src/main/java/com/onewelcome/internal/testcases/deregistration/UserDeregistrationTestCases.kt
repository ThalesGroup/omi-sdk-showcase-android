package com.onewelcome.internal.testcases.deregistration

import com.onewelcome.core.usecase.DeregisterUserUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class UserDeregistrationTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val deregisterUserUseCase: DeregisterUserUseCase
) {

  val tests = TestCategory(
    name = "User deregistration",
    testCases = listOf()
  )

  private suspend fun deregisterUser(): TestStatus {
    TODO("add this test once custom registration is implemented, so there's a user profile registered. AOSA-26")
  }
}
