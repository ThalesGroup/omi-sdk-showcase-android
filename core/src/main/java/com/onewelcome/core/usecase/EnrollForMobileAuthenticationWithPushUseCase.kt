package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthWithPushEnrollmentHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthWithPushEnrollmentError
import com.onewelcome.core.facade.FirebaseMessagingFacade
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class EnrollForMobileAuthenticationWithPushUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
  private val firebaseMessagingFacade: FirebaseMessagingFacade
) {

  suspend fun execute(): Result<Unit, Throwable> {
    return firebaseMessagingFacade.getToken()
      .flatMap { enroll(it) }
  }

  private suspend fun enroll(registrationToken: String): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient()
          .enrollUserForMobileAuthWithPush(registrationToken, object : OneginiMobileAuthWithPushEnrollmentHandler {
            override fun onError(error: OneginiMobileAuthWithPushEnrollmentError) {
              continuation.resume(Err(error))
            }

            override fun onSuccess() {
              continuation.resume(Ok(Unit))
            }
          })
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
