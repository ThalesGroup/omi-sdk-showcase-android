package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthEnrollmentHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthEnrollmentError
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class EnrollForMobileAuthenticationUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade
) {

  suspend fun execute(): Result<Unit, Throwable> = runCatching {
    val oneginiClient = omiSdkFacade.getOneginiClientNew()
    return suspendCancellableCoroutine { continuation ->
      oneginiClient.getUserClient().enrollUserForMobileAuth(object : OneginiMobileAuthEnrollmentHandler {
        override fun onError(error: OneginiMobileAuthEnrollmentError) {
          continuation.resume(Err(error))
        }

        override fun onSuccess() {
          continuation.resume(Ok(Unit))
        }
      })
    }
  }.onFailure {
    Err(it)
  }
}
