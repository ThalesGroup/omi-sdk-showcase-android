package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.MobileAuthEnrollmentStatus
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetMobileAuthenticationEnrollmentStatusUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
  private val getUserProfilesUseCase: GetUserProfilesUseCase
) {

  suspend fun execute(): Result<List<MobileAuthEnrollmentStatus>, Throwable> {
    return getUserProfilesUseCase.execute()
      .map { getEnrollmentStatus(it) }
  }

  private fun getEnrollmentStatus(userProfiles: Set<UserProfile>): List<MobileAuthEnrollmentStatus> {
    return userProfiles.map {
      MobileAuthEnrollmentStatus(
        userProfile = it,
        isEnrolledForMobileAuth = omiSdkFacade.oneginiClient.getUserClient().isUserEnrolledForMobileAuth(it),
        isEnrolledForMobileAuthWithPush = omiSdkFacade.oneginiClient.getUserClient().isUserEnrolledForMobileAuthWithPush(it)
      )
    }
  }
}
