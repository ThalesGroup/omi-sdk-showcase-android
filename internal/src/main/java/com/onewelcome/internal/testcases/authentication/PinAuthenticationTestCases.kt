package com.onewelcome.internal.testcases.authentication

import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject


class PinAuthenticationTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase
) {
  val tests = TestCategory(
    name = "Pin authentication",
    testCases = listOf(
      TestCase(
        name = "getRegisteredAuthenticators",
        testFunction = ::getRegisteredAuthenticators
      ),
      TestCase(
        name = "getAuthenticatedUserProfile",
        testFunction = ::getAuthenticatedUserProfile
      )
    )
  )

  private suspend fun getRegisteredAuthenticators(): TestStatus {
    val userClient = getUserProfilesUseCase.execute().value.first()
    val result = getRegisteredAuthenticatorsUseCase.execute(userClient)
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }

  private suspend fun getAuthenticatedUserProfile(): TestStatus {
    val result = getAuthenticatedUserProfileUseCase.execute()
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }
}