package com.onewelcome.core.usecase

import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import javax.inject.Inject

class AuthenticateWithPushUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  fun execute(userProfile: UserProfile, mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler) {
//    omiSdkFacade.oneginiClient.getUserClient().handleMobileAuthWithPushRequest()
  }
}
