package com.onewelcome.showcaseapp.usecase

import com.onewelcome.showcaseapp.omisdk.OmiSdkEngine
import com.onewelcome.showcaseapp.omisdk.OmiSdkInitializationHandler
import javax.inject.Inject

class OmiSdkInitializationUseCase @Inject constructor(
  private val omiSdkEngine: OmiSdkEngine,
  private val omiSdkInitializationHandler: OmiSdkInitializationHandler
) {
  fun initialize() {
    omiSdkEngine.omiSdk.start(omiSdkInitializationHandler)
  }
}
