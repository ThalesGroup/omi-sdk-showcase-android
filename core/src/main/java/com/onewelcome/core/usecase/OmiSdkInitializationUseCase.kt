package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class OmiSdkInitializationUseCase @Inject constructor(
  private val omiSdkEngine: OmiSdkFacade
) {

  suspend fun initialize(settings: OmiSdkInitializationSettings): Result<Set<UserProfile>, OneginiInitializationError> {
    return omiSdkEngine.initialize(settings)
  }
}
