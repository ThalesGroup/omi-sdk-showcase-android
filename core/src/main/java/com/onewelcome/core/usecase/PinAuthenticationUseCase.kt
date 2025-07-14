package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
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

class PinAuthenticationUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
) {

  suspend fun authenticate(userProfile: UserProfile): Result<Pair<UserProfile, CustomInfo?>, Throwable> {
    val authenticatorsResult = getRegisteredAuthenticatorsUseCase.getRegisteredAuthenticators(userProfile)
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        authenticatorsResult
          .onSuccess {
            val pinAuthenticator = it.first { it.type == OneginiAuthenticator.Type.PIN }
            omiSdkFacade.oneginiClient.getUserClient().authenticateUser(
              userProfile = userProfile,
              oneginiAuthenticator = pinAuthenticator,
              authenticationHandler = object : OneginiAuthenticationHandler {
                override fun onSuccess(
                  userProfile: UserProfile,
                  customInfo: CustomInfo?
                ) {
                  continuation.resume(Ok(Pair(userProfile, customInfo)))
                }

                override fun onError(error: OneginiAuthenticationError) {
                  continuation.resume(Err(error))
                }

              }
            )
          }.onFailure { continuation.resume(Err(it)) }
      }.onFailure { continuation.resume(Err(it)) }
    }
  }
}