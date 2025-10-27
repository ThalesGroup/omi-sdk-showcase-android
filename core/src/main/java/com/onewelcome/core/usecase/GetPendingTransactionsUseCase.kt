package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiPendingMobileAuthWithPushRequestsHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiPendingMobileAuthWithPushRequestError
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class GetPendingTransactionsUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(): Result<Set<OneginiMobileAuthWithPushRequest>, OneginiPendingMobileAuthWithPushRequestError> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().getPendingMobileAuthWithPushRequests(
          object : OneginiPendingMobileAuthWithPushRequestsHandler {
            override fun onSuccess(pendingMobileAuthWithPushRequests: Set<OneginiMobileAuthWithPushRequest>) {
              continuation.resume(Ok(pendingMobileAuthWithPushRequests))
            }

            override fun onError(error: OneginiPendingMobileAuthWithPushRequestError) {
              continuation.resume(Err(error))
            }
          }
        )
      }
    }
  }
}
