package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.OmiSdkEngine
import javax.inject.Inject

class GetAuthenticatorsUseCase @Inject constructor(private val omiSdkEngine: OmiSdkEngine) {

  fun execute(userProfile: UserProfile): Result<Set<OneginiAuthenticator>, Throwable> {
    return runCatching {
      omiSdkEngine.oneginiClient.getUserClient().getAllAuthenticators(userProfile)
    }
  }
}
