package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticatorRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticatorRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class RegisterAuthenticatorUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  suspend fun execute(authenticator: OneginiAuthenticator): Result<CustomInfo?, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().registerAuthenticator(authenticator, object : OneginiAuthenticatorRegistrationHandler {
          override fun onSuccess(customInfo: CustomInfo?) {
            continuation.resume(Ok(customInfo))
          }

          override fun onError(error: OneginiAuthenticatorRegistrationError) {
            continuation.resume(Err(error))
          }
        })
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
