package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class IsUserEnrolledForMobileAuthWithPushUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade
) {

  fun execute(userProfile: UserProfile): Result<Boolean, Throwable> {
    return runCatching {
      omiSdkFacade.oneginiClient.getUserClient().isUserEnrolledForMobileAuthWithPush(userProfile)
    }.onFailure { Err(it) }
  }
}
