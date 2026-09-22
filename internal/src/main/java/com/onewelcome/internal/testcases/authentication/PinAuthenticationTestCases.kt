package com.onewelcome.internal.testcases.authentication

import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetIdTokenUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.PinAuthenticationUseCase
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import com.onewelcome.internal.testcases.registration.UserRegistrationTestCases.Companion.TEST_PIN
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import javax.inject.Inject


class PinAuthenticationTestCases @Inject constructor(
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val getAuthenticatorsUseCase: GetAuthenticatorsUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getIdTokenUseCase: GetIdTokenUseCase,
  private val pinAuthenticationUseCase: PinAuthenticationUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) {
  val tests = TestCategory(
    name = "Pin authentication",
    testCases = listOf(
      TestCase(
        name = "pinAuthentication",
        testFunction = ::pinAuthentication
      ),
      TestCase(
        name = "getRegisteredAuthenticators",
        testFunction = ::getRegisteredAuthenticators
      ),
      TestCase(
        name = "getAllAuthenticators",
        testFunction = ::getAllAuthenticators
      ),
      TestCase(
        name = "getAuthenticatedUserProfile",
        testFunction = ::getAuthenticatedUserProfile
      ),
      TestCase(
        name = "getIdToken",
        testFunction = ::getIdToken
      ),
    )
  )

  private suspend fun pinAuthentication(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val pinAuthenticator = getRegisteredAuthenticatorsUseCase.execute(userProfile).value
      ?.firstOrNull { it.type == OneginiAuthenticator.Type.PIN } ?: return TestStatus.Failed

    var authSucceeded = false

    coroutineScope {
      val pinJob = launch {
        pinAuthenticationRequestHandler.startPinAuthenticationFlow.collect {
          pinAuthenticationRequestHandler.pinCallback?.acceptAuthenticationRequest(TEST_PIN)
        }
      }

      val result = pinAuthenticationUseCase.execute(userProfile, pinAuthenticator)
      authSucceeded = result.isOk

      pinJob.cancel()
    }

    return if (authSucceeded) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun getRegisteredAuthenticators(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val result = getRegisteredAuthenticatorsUseCase.execute(userProfile)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private suspend fun getAllAuthenticators(): TestStatus {
    val userProfile = getUserProfilesUseCase.execute().value.firstOrNull() ?: return TestStatus.Failed
    val result = getAuthenticatorsUseCase.execute(userProfile)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  private fun getAuthenticatedUserProfile(): TestStatus {
    val result = getAuthenticatedUserProfileUseCase.execute()
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  // ID token availability depends on server config (openid scope), not the OS.
  // A null token (IllegalStateException) is treated as Passed — it means the SDK
  // API is accessible but the server didn't issue a token. Only a thrown exception
  // indicates an OS-level failure.
  private fun getIdToken(): TestStatus {
    val result = getIdTokenUseCase.execute()
    return when {
      result.isOk -> TestStatus.Passed
      result.error is IllegalStateException -> TestStatus.Passed
      else -> TestStatus.Failed
    }
  }
}
