package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class GetRegisteredAuthenticatorsUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
) {
  suspend fun execute(userProfile: UserProfile): Result<Set<OneginiAuthenticator>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        val authenticators = omiSdkFacade.oneginiClient.getUserClient().getRegisteredAuthenticators(userProfile)
        continuation.resume(Ok(authenticators))
      }.onFailure {
        continuation.resume(Err(it))
      }
    }
  }
}
