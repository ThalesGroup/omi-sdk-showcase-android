package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiStatelessRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class StatelessUserRegistrationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  suspend fun execute(
    identityProvider: OneginiIdentityProvider?,
    scopes: List<String>
  ): Result<CustomInfo?, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().registerStatelessUser(
          identityProvider = identityProvider,
          scopes = scopes.toTypedArray(),
          statelessRegistrationHandler = object : OneginiStatelessRegistrationHandler {
            override fun onSuccess(customInfo: CustomInfo?) {
              continuation.resume(Ok(customInfo))
            }

            override fun onError(error: OneginiRegistrationError) {
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
