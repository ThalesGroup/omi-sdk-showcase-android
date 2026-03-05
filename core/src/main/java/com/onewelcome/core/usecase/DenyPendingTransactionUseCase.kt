package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiDenyMobileAuthWithPushRequestHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiDenyMobileAuthWithPushRequestError
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class DenyPendingTransactionUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(request: OneginiMobileAuthWithPushRequest): Result<Unit, OneginiDenyMobileAuthWithPushRequestError> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().denyMobileAuthWithPushRequest(
          request,
          object : OneginiDenyMobileAuthWithPushRequestHandler {
            override fun onSuccess() {
              continuation.resume(Ok(Unit))
            }

            override fun onError(error: OneginiDenyMobileAuthWithPushRequestError) {
              continuation.resume(Err(error))
            }
          }
        )
      }
    }
  }
}
