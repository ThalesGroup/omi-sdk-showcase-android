package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetIdTokenUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  fun execute(): Result<String, Throwable> {
    return runCatching {
      val idToken = omiSdkFacade.oneginiClient.getUserClient().idToken
      if (idToken != null) {
        Ok(idToken)
      } else {
        Err(IllegalStateException("ID token is not available. User must be registered with OpenID scope and authenticated."))
      }
    }.getOrElse { Err(it) }
  }
}
