package com.onewelcome.core.usecase

import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class AuthenticateWithPushUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  fun execute(pushRequest: OneginiMobileAuthWithPushRequest) {
    omiSdkFacade.oneginiClient.getUserClient().handleMobileAuthWithPushRequest(pushRequest, object : OneginiMobileAuthenticationHandler {
      override fun onSuccess(customInfo: CustomInfo?) {
        // onSuccess powinien informować TransactionConfirmationScreen że
      }

      override fun onError(error: OneginiMobileAuthenticationError) {
        // OnError powinien informować TransactionConfirmationScreen że coś poszło nietak
      }
    })
  }
}
