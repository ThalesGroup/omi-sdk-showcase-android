package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthWithOtpHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthWithOtpError
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthenticateWithOtpUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  suspend fun invoke(otp: String): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().handleMobileAuthWithOtp(otp, object : OneginiMobileAuthWithOtpHandler{
          override fun onSuccess() {
            continuation.resume(Ok(Unit))
          }

          override fun onError(error: OneginiMobileAuthWithOtpError) {
            continuation.resume(Err(error))
          }

        })
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
