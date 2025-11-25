package com.onewelcome.core.usecase

import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.OneginiAppToWebSingleSignOnHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAppToWebSingleSignOnError
import com.onegini.mobile.sdk.android.model.OneginiAppToWebSingleSignOn
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class SingleSignOnUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  suspend fun execute(targetUri: Uri): Result<OneginiAppToWebSingleSignOn, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().getAppToWebSingleSignOn(
          targetUri,
          object : OneginiAppToWebSingleSignOnHandler {
            override fun onSuccess(result: OneginiAppToWebSingleSignOn) {
              continuation.resume(Ok(result))
            }

            override fun onError(error: OneginiAppToWebSingleSignOnError) {
              continuation.resume(Err(error))
            }
          }
        )
      }.onFailure { continuation.resume(Err(it)) }
    }
  }
}
