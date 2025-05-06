package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class GetUserProfilesUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun execute(): Result<Set<UserProfile>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching { omiSdkFacade.oneginiClient.getUserClient().userProfiles }
        .onSuccess { continuation.resume(Ok(it)) }
        .onFailure { continuation.resume(Err(it)) }
    }
  }
}
