package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

import com.onegini.mobile.sdk.android.handlers.OneginiChangePinHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiChangePinError
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ChangePinUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().changePin(
          object : OneginiChangePinHandler {
            override fun onSuccess() {
              continuation.resume(Ok(Unit))
            }

            override fun onError(error: OneginiChangePinError) {
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure { continuation.resume(Err(it)) }
    }
  }
}
