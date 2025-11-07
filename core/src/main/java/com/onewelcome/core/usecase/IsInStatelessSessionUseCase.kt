package com.onewelcome.core.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class IsInStatelessSessionUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  fun execute(): Result<Boolean, Throwable> {
    return runCatching {
      hasActiveAccessToken() && hasNoAuthenticatedUser()
    }
  }

  private fun hasActiveAccessToken(): Boolean {
    return omiSdkFacade.oneginiClient.getUserClient().accessToken != null
  }

  private fun hasNoAuthenticatedUser(): Boolean {
    return omiSdkFacade.oneginiClient.getUserClient().authenticatedUserProfile == null
  }
}
