package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.notification.NotificationEventDispatcher
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class AuthenticateWithPushUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
  private val notificationEventDispatcher: NotificationEventDispatcher,
) {
  suspend fun execute(pushRequest: OneginiMobileAuthWithPushRequest): Result<Unit, Throwable> {
    return runCatching {
      omiSdkFacade.getOneginiClientNew().getUserClient()
        .handleMobileAuthWithPushRequest(pushRequest, object : OneginiMobileAuthenticationHandler {
          override fun onSuccess(customInfo: CustomInfo?) {
            notificationEventDispatcher.send(Ok(customInfo))
          }

          override fun onError(error: OneginiMobileAuthenticationError) {
            notificationEventDispatcher.send(Err(error))
          }
        })
    }
  }
}
