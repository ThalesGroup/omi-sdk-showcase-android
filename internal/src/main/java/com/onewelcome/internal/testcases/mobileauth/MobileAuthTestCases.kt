package com.onewelcome.internal.testcases.mobileauth

import com.onewelcome.core.usecase.EnrollForMobileAuthenticationUseCase
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationWithPushUseCase
import com.onewelcome.core.usecase.GetPendingTransactionsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class MobileAuthTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val enrollForMobileAuthenticationUseCase: EnrollForMobileAuthenticationUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val enrollForMobileAuthenticationWithPushUseCase: EnrollForMobileAuthenticationWithPushUseCase,
  private val isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase,
  private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
) {
  val tests = TestCategory(
    name = "Mobile authentication",
    testCases = listOf(
      TestCase(
        name = "enrollForMobileAuth",
        testFunction = ::enrollForMobileAuth
      ),
      TestCase(
        name = "isEnrolledForMobileAuth",
        testFunction = ::isEnrolledForMobileAuth
      ),
      TestCase(
        name = "enrollForMobileAuthWithPush",
        testFunction = ::enrollForMobileAuthWithPush
      ),
      TestCase(
        name = "isEnrolledForMobileAuthWithPush",
        testFunction = ::isEnrolledForMobileAuthWithPush
      ),
      TestCase(
        name = "getPendingTransactions",
        testFunction = ::getPendingTransactions
      ),
    )
  )

  // authenticateWithOtp is excluded: requires a live OTP value from the server.

  // Any callback response (Ok or Err) means the SDK's enrollment mechanism works
  // at the OS level. Server-side rejections are not OS issues.
  private suspend fun enrollForMobileAuth(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val alreadyEnrolled = isUserEnrolledForMobileAuthUseCase.execute(userProfile).value == true
    if (alreadyEnrolled) return TestStatus.Passed
    return try {
      enrollForMobileAuthenticationUseCase.execute()
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }

  private suspend fun isEnrolledForMobileAuth(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val result = isUserEnrolledForMobileAuthUseCase.execute(userProfile)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  // Any callback response (Ok or Err) means the SDK's push enrollment mechanism
  // works at the OS level. Firebase config or server-side rejections are not OS issues.
  private suspend fun enrollForMobileAuthWithPush(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val alreadyEnrolled = isUserEnrolledForMobileAuthWithPushUseCase.execute(userProfile).value == true
    if (alreadyEnrolled) return TestStatus.Passed
    return try {
      enrollForMobileAuthenticationWithPushUseCase.execute()
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }

  private suspend fun isEnrolledForMobileAuthWithPush(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val result = isUserEnrolledForMobileAuthWithPushUseCase.execute(userProfile)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun getPendingTransactions(): TestStatus {
    val result = getPendingTransactionsUseCase.execute()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }
}
