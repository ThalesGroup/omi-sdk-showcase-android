package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiRefreshMobileAuthPushTokenHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRefreshMobileAuthPushTokenError
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class RefreshMobileAuthPushTokenUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade
) {

  suspend fun execute(token: String): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getDeviceClient().refreshMobileAuthPushToken(token, object : OneginiRefreshMobileAuthPushTokenHandler {
          override fun onError(error: OneginiRefreshMobileAuthPushTokenError) {
            continuation.resume(Err(error))
          }

          override fun onSuccess() {
            continuation.resume(Ok(Unit))
          }
        })
      }
    }
  }
}
