package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticationError
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.CustomAuthenticator
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


class CustomAuthenticationUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade
) {

  suspend fun execute(userProfile: UserProfile): Result<CustomInfo?, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        // Find the custom authenticator
        val customAuthenticator = findCustomAuthenticator(userProfile)
          ?: throw IllegalStateException("Custom authenticator not found or not registered for this user")

        omiSdkFacade.oneginiClient.getUserClient().authenticateUser(
          userProfile,
          customAuthenticator,
          object : OneginiAuthenticationHandler {
            override fun onSuccess(userProfile: UserProfile, customInfo: CustomInfo?) {
              continuation.resume(Ok(customInfo))
            }

            override fun onError(error: OneginiAuthenticationError) {
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }

  private fun findCustomAuthenticator(userProfile: UserProfile): OneginiAuthenticator? {
    val registeredAuthenticators = omiSdkFacade.oneginiClient.getUserClient()
      .getRegisteredAuthenticators(userProfile)
    return registeredAuthenticators.find {
      it.id == CustomAuthenticator.CUSTOM_AUTHENTICATOR_ID
    }
  }
}
