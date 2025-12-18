package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiImplicitAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiImplicitTokenRequestError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class ImplicitAuthenticationUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
) {
  suspend fun execute(
    userProfile: UserProfile,
    scopes: Array<String?>?,
  ): Result<UserProfile, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().authenticateUserImplicitly(
          userProfile = userProfile,
          scopes = scopes,
          implicitAuthenticationHandler = object : OneginiImplicitAuthenticationHandler {
            override fun onSuccess(userProfile: UserProfile) {
              continuation.resume(Ok(userProfile))
            }

            override fun onError(error: OneginiImplicitTokenRequestError) {
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure { continuation.resume(Err(it)) }
    }
  }
}
