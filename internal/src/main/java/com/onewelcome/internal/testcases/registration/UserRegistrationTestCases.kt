package com.onewelcome.internal.testcases.registration

import com.onewelcome.core.omisdk.entity.TwoStepIdentityProvider
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.TwoStepRegistrationRequestHandler
import com.onewelcome.core.usecase.GetTwoStepIdentityProvidersUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsInStatelessSessionUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRegistrationTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val isInStatelessSessionUseCase: IsInStatelessSessionUseCase,
  private val userRegistrationUseCase: UserRegistrationUseCase,
  private val getTwoStepIdentityProvidersUseCase: GetTwoStepIdentityProvidersUseCase,
  private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler,
  private val createPinRequestHandler: CreatePinRequestHandler,
) {
  val tests = TestCategory(
    name = "User registration",
    testCases = listOf(
      TestCase(
        name = "registerUser",
        testFunction = ::registerUser
      ),TestCase(
            name = "getUserProfiles",
            testFunction = ::getUserProfiles
        ),
        TestCase(
            name = "isInStatelessSession",
            testFunction = ::isInStatelessSession
        ),
    )
  )

  private suspend fun registerUser(): TestStatus {
    // Skip if a user is already registered on the device
    val profiles = getUserProfilesUseCase.execute()
    if (profiles.isOk && profiles.value.isNotEmpty()) return TestStatus.Passed
    val providersResult = getTwoStepIdentityProvidersUseCase.execute()
    if (!providersResult.isOk) return TestStatus.Failed
    val twoStepProvider = providersResult.value.find { it.id == TwoStepIdentityProvider.ID } ?: return TestStatus.Failed
    var registrationSucceeded = false
    coroutineScope {
      // Auto-respond to the finishRegistration two-step challenge with the expected code.
      // initRegistration submits "12345"; the Extension Engine expects the same code in step 2.
      // Uses collect{} (not first()) because startTwoStepInputFlow has replay=1: a stale
      // value from a previous cancelled run could fire immediately and consume first(),
      // leaving the real finishRegistration callback unhandled. With collect{}, after the
      // stale fire submitResponseCode is a no-op (null callback) and cleanUp() resets the
      // replay cache so the real emission is caught on the next collect iteration.
      val twoStepJob = launch {
        twoStepRegistrationRequestHandler.startTwoStepInputFlow.collect {
          twoStepRegistrationRequestHandler.submitResponseCode("12345")
        }
      }
      // Auto-supply PIN when the SDK initially asks for it during registration.
      val pinJob = launch {
        createPinRequestHandler.startPinCreationFlow.collect {
          createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(TEST_PIN)
        }
      }
        // Handle PIN policy rejection — when the SDK rejects the PIN it calls
      // onNextPinCreationAttempt() and keeps the SAME pinCallback alive without
      // calling startPinCreation() again. pinJob never gets a new startPinCreationFlow
      // event and deadlocks. This job catches the validation error and re-submits
      // the PIN on the same callback, mirroring CreatePinInputViewModel behaviour.
      val pinRetryJob = launch {
        createPinRequestHandler.pinValidationErrorFlow.collect { error ->
          createPinRequestHandler.pinCallback?.acceptAuthenticationRequest(TEST_PIN)
        }
      }
      val result = userRegistrationUseCase.register(twoStepProvider, Constants.DEFAULT_SCOPES)
      registrationSucceeded = result.isOk
      // Cancel any job still waiting (e.g. registration failed before reaching that step).
      twoStepJob.cancel()
      pinJob.cancel()
      pinRetryJob.cancel()
    }

    return if (registrationSucceeded) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun getUserProfiles(): TestStatus {
    val result = getUserProfilesUseCase.execute()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun isInStatelessSession(): TestStatus {
    val result = isInStatelessSessionUseCase.execute()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  companion object {
    // PIN auto-supplied during two-step registration.
    // Computed property (not a stored val) so each access returns a fresh array —
    // the SDK zeroes the array after use for security, which would corrupt a reused instance.
    val TEST_PIN get() = charArrayOf('1', '4', '7', '2', '5')
  }
}
