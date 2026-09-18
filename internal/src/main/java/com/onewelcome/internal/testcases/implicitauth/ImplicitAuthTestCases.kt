package com.onewelcome.internal.testcases.implicitauth

import com.onewelcome.core.usecase.GetImplicitlyAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.ImplicitAuthenticationUseCase
import com.onewelcome.core.usecase.resourcecall.ImplicitResourceCallUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class ImplicitAuthTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val implicitAuthenticationUseCase: ImplicitAuthenticationUseCase,
  private val getImplicitlyAuthenticatedUserProfileUseCase: GetImplicitlyAuthenticatedUserProfileUseCase,
  private val implicitResourceCallUseCase: ImplicitResourceCallUseCase,
) {
  val tests = TestCategory(
    name = "Implicit authentication",
    testCases = listOf(
      TestCase(
        name = "implicitAuthentication",
        testFunction = ::implicitAuthentication
      ),
      TestCase(
        name = "getImplicitlyAuthenticatedUserProfile",
        testFunction = ::getImplicitlyAuthenticatedUserProfile
      ),
      TestCase(
        name = "implicitResourceCall",
        testFunction = ::implicitResourceCall
      ),
    )
  )

  private suspend fun implicitAuthentication(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val result = implicitAuthenticationUseCase.execute(userProfile, scopes = null)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private fun getImplicitlyAuthenticatedUserProfile(): TestStatus {
    val result = getImplicitlyAuthenticatedUserProfileUseCase.execute()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun implicitResourceCall(): TestStatus {
    val result = implicitResourceCallUseCase.getUserId()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }
}
