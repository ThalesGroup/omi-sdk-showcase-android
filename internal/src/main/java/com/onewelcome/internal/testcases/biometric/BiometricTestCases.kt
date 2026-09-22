package com.onewelcome.internal.testcases.biometric

import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetBiometricAuthenticatorStatusUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class BiometricTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getAuthenticatorsUseCase: GetAuthenticatorsUseCase,
  private val getBiometricAuthenticatorStatusUseCase: GetBiometricAuthenticatorStatusUseCase,
) {
  val tests = TestCategory(
    name = "Biometric",
    testCases = listOf(
      TestCase(
        name = "getBiometricAuthenticatorStatus",
        testFunction = ::getBiometricAuthenticatorStatus
      ),
    )
  )
  private suspend fun getBiometricAuthenticatorStatus(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val authenticatorsResult = getAuthenticatorsUseCase.execute(userProfile)
    if (!authenticatorsResult.isOk) return TestStatus.Failed
    return try {
      getBiometricAuthenticatorStatusUseCase.execute(authenticatorsResult.value)
      TestStatus.Passed
    } catch (e: Exception) {
      TestStatus.Failed
    }
  }
}
