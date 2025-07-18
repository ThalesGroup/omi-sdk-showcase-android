package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiDeregisterUserProfileHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiDeregistrationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class DeregisterUserUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  suspend fun execute(userProfile: UserProfile): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().deregisterUser(userProfile, object : OneginiDeregisterUserProfileHandler {
          override fun onSuccess() {
            continuation.resume(Ok(Unit))
          }

          override fun onError(error: OneginiDeregistrationError) {
            continuation.resume(Err(error))
          }
        })
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
