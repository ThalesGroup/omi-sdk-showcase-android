package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class GetBrowserIdentityProvidersUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
) {
  suspend fun execute(): Result<Set<OneginiIdentityProvider>, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      runCatching {
        omiSdkFacade.oneginiClient.getUserClient().identityProviders
          .filter { it.toString().contains(BROWSER_IDENTITY_PROVIDER) }
          .toSet()
      }
        .onSuccess { continuation.resume(Ok(it)) }
        .onFailure { continuation.resume(Err(it)) }
    }
  }

  companion object {
    private const val BROWSER_IDENTITY_PROVIDER = "BrowserIdentityProvider"
  }
}
