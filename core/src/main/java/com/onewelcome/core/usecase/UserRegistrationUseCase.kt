package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class UserRegistrationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun register(
    identityProvider: OneginiIdentityProvider?,
    scopes: List<String>
  ): Result<Pair<UserProfile, CustomInfo?>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().registerUser(
          identityProvider = identityProvider,
          scopes = scopes.toTypedArray(),
          registrationHandler = object : OneginiRegistrationHandler {
            override fun onSuccess(
              userProfile: UserProfile,
              customInfo: CustomInfo?
            ) {
              continuation.resume(Ok(Pair(userProfile, customInfo)))
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
