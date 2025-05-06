package com.onewelcome.core.usecase

import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.BrowserIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class BrowserRegistrationUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
  private val browserRegistrationRequestHandler: BrowserRegistrationRequestHandler
) {
  suspend fun getBrowserIdentityProviders(): Result<List<BrowserIdentityProvider>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().identityProviders
          .filter { it.toString().startsWith(BROWSER_IDENTITY_PROVIDER) }
          .map { BrowserIdentityProvider(it.name, it.id) }
      }
        .onSuccess { continuation.resume(Ok(it)) }
        .onFailure { continuation.resume(Err(it)) }
    }
  }

  suspend fun register(
    identityProvider: BrowserIdentityProvider?,
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
              Log.d("register onSuccess", "userProfile: $userProfile customInfo: $customInfo")
              continuation.resume(Ok(Pair(userProfile, customInfo)))
            }

            override fun onError(error: OneginiRegistrationError) {
              Log.d("register onError", "error: $error")
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure { continuation.resume(Err(it)) }
    }
  }

  fun cancelRegistration() {
    browserRegistrationRequestHandler.cancelRegistration()
  }

  companion object {
    private const val BROWSER_IDENTITY_PROVIDER = "BrowserIdentityProvider"
  }
}
