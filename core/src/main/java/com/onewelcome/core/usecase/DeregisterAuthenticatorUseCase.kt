package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticatorDeregistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticatorDeregistrationError
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onewelcome.core.omisdk.OmiSdkEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class DeregisterAuthenticatorUseCase @Inject constructor(private val omiSdkEngine: OmiSdkEngine) {

  suspend fun execute(authenticator: OneginiAuthenticator): Result<Unit, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkEngine.oneginiClient.getUserClient()
          .deregisterAuthenticator(authenticator, object : OneginiAuthenticatorDeregistrationHandler {
            override fun onSuccess() {
              continuation.resume(Ok(Unit))
            }

            override fun onError(error: OneginiAuthenticatorDeregistrationError) {
              continuation.resume(Err(error))
            }
          })
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}

