package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetTwoStepIdentityProvidersUseCase @Inject constructor(
  private val omiSdkFacade: OmiSdkFacade
) {
  fun execute(): Result<Set<OneginiIdentityProvider>, Throwable> {
    return try {
      val allIdentityProviders = omiSdkFacade.oneginiClient.getUserClient().identityProviders
      //POC-START
      Ok(allIdentityProviders)
      //POC-END
    } catch (e: Exception) {
      Err(e)
    }
  }
}
