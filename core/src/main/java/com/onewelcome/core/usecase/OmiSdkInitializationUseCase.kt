package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.OmiSdkInitializationSettings
import com.onewelcome.core.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class OmiSdkInitializationUseCase @Inject constructor(
  private val omiSdkEngine: OmiSdkFacade
) {

  suspend fun initialize(settings: OmiSdkInitializationSettings): Result<Set<UserProfile>, OneginiInitializationError> {
    return suspendCancellableCoroutine { continuation ->
      omiSdkEngine.initialize(settings)
      omiSdkEngine.oneginiClient.start(object : OneginiInitializationHandler {
        override fun onSuccess(removedUserProfiles: Set<UserProfile>) {
          continuation.resume(Ok(removedUserProfiles))
        }

        override fun onError(error: OneginiInitializationError) {
          continuation.resume(Err(error))
        }
      })
    }
  }
}
