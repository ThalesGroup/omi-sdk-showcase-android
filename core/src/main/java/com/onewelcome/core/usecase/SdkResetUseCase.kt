package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.OneginiResetHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiResetError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class SdkResetUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.reset(object : OneginiResetHandler {
          override fun onSuccess(removedUserProfiles: Set<UserProfile>) {
            continuation.resume(Ok(Unit))
          }

          override fun onError(error: OneginiResetError) {
            continuation.resume(Err(error))
          }
        })
      }.onFailure { continuation.resume(Err(it)) }
    }
  }
}
