package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetCustomIdentityProvidersUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade,
) {
  fun execute(): Result<Set<OneginiIdentityProvider>, Throwable> =
    runCatching {
      omiSdkFacade.oneginiClient.getUserClient().identityProviders
        .filter { it.toString().contains( API_IDENTITY_PROVIDER) }
        .toSet()
    }

  companion object {
      private const val API_IDENTITY_PROVIDER = "ApiIdentityProvider"
  }
}
