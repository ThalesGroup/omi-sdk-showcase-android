package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticationError
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class BiometricAuthenticationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  suspend fun execute(userProfile: UserProfile): Result<Pair<UserProfile, CustomInfo?>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        val biometricAuthenticator = userProfile.getBiometricAuthenticator()
        if (biometricAuthenticator == null) {
          continuation.resume(Err(Exception("Biometric authenticator not found for user")))
          return@suspendCancellableCoroutine
        }

        omiSdkFacade.oneginiClient.getUserClient()
          .authenticateUser(userProfile, biometricAuthenticator, object : OneginiAuthenticationHandler {
            override fun onSuccess(userProfile: UserProfile, customInfo: CustomInfo?) {
              continuation.resume(Ok(userProfile to customInfo))
            }

            override fun onError(error: OneginiAuthenticationError) {
              continuation.resume(Err(error))
            }
          })
      }
    }
  }

  private fun UserProfile.getBiometricAuthenticator() =
    omiSdkFacade.oneginiClient.getUserClient().getAllAuthenticators(this).find { it.type == OneginiAuthenticator.Type.BIOMETRIC }
}
