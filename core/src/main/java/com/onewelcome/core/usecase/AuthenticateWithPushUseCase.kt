package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticateWithPushUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  private val _authenticationEvent = Channel<Result<CustomInfo?, OneginiMobileAuthenticationError>>(Channel.BUFFERED)
  val authenticationEvent = _authenticationEvent.receiveAsFlow()

  fun execute(pushRequest: OneginiMobileAuthWithPushRequest) {
    omiSdkFacade.oneginiClient.getUserClient().handleMobileAuthWithPushRequest(pushRequest, object : OneginiMobileAuthenticationHandler {
      override fun onSuccess(customInfo: CustomInfo?) {
        _authenticationEvent.trySend(Ok(customInfo))
      }

      override fun onError(error: OneginiMobileAuthenticationError) {
        _authenticationEvent.trySend(Err(error))
      }
    })
  }
}
