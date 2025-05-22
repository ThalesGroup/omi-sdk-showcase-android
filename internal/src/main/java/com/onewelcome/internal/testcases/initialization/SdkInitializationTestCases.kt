package com.onewelcome.internal.testcases.initialization

import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.core.util.TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import javax.inject.Inject

class SdkInitializationTestCases @Inject constructor(
  private val sdkInitializationUseCase: OmiSdkInitializationUseCase,
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
    sdkInitializationUseCase.initialize(TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    val result = sdkInitializationUseCase.initialize(TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    return if (result.isOk) {
      TestStatus.Passed
    } else {
      TestStatus.Failed
    }
  }
}
