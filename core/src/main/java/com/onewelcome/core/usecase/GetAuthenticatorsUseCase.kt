package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetAuthenticatorsUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  fun execute(userProfile: UserProfile): Result<Set<OneginiAuthenticator>, Throwable> {
    return runCatching {
      omiSdkFacade.oneginiClient.getUserClient().getAllAuthenticators(userProfile)
    }
  }
}
