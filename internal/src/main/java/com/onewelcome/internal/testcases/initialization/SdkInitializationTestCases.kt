package com.onewelcome.internal.testcases.initialization

import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.TwoStepRegistrationRequestHandler
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.core.util.TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class SdkInitializationTestCases @Inject constructor(
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase,
  private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler,
  private val createPinRequestHandler: CreatePinRequestHandler,
) {
  val tests = TestCategory(
    name = "SDK initialization",
    testCases = listOf(
      TestCase(
        name = "sdkInitialized",
        testFunction = ::sdkInitialized
      ),
    )
  )

  private suspend fun sdkInitialized(): TestStatus {
    // If a previous registration attempt left the SDK with an unresolved two-step or
    // PIN callback, re-initialization will fail with error 10000 on BOTH calls (not just
    // the first). Cancel any pending state before attempting to re-initialize.
    if (twoStepRegistrationRequestHandler.isTwoStepregistrationInProgress()) {
      twoStepRegistrationRequestHandler.cancelRegistration()
    }
    if (createPinRequestHandler.isPinCreationInProgress()) {
      createPinRequestHandler.cancelPinCreation()
    }
    // First call absorbs the client-validation error that occurs when the SDK is already
    // running from app startup. The second call is the real test and succeeds cleanly.
    // Both calls use the full handler set so subsequent tests (two-step registration, etc.)
    // find a properly configured SDK instance.
    sdkInitializationUseCase.initialize(OS_COMPAT_SDK_INITIALIZATION_SETTINGS)
    val result = sdkInitializationUseCase.initialize(OS_COMPAT_SDK_INITIALIZATION_SETTINGS)
    return if (result.isOk) TestStatus.Passed else TestStatus.Failed
  }

  companion object {
    val OS_COMPAT_SDK_INITIALIZATION_SETTINGS = OmiSdkInitializationSettings(
        shouldStoreCookies = true,
        httpConnectTimeout = null,
        httpReadTimeout = null,
        deviceConfigCacheDuration = null,
        handlers = listOf(
            HandlerType.BROWSER_REGISTRATION,
            HandlerType.TWO_STEP_REGISTRATION,
            HandlerType.BIOMETRIC_AUTHENTICATION,
            HandlerType.MOBILE_AUTH_WITH_PUSH,
            HandlerType.MOBILE_AUTH_WITH_PUSH_PIN,
            HandlerType.MOBILE_AUTH_WITH_PUSH_BIOMETRIC,
            HandlerType.MOBILE_AUTH_WITH_OTP,
        ),
    )
  }
}
