package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetUserProfilesUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(): Result<Set<UserProfile>, Throwable> {
    return runCatching { omiSdkFacade.getOneginiClientNew().getUserClient().userProfiles }
  }
}
