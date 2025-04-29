package com.onewelcome.core.usecase

import com.onewelcome.core.omisdk.entity.IdentityProvider.BrowserIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class BrowserRegistrationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  val browserIdentityProviders: Result<List<BrowserIdentityProvider>> = runCatching {
    omiSdkFacade.oneginiClient.getUserClient().identityProviders
      .filter { it.toString().startsWith(BROWSER_IDENTITY_PROVIDER) }
      .map { BrowserIdentityProvider(it.name, it.id) }
  }

  fun register() {
    val browserProviders = omiSdkFacade.oneginiClient.getUserClient().identityProviders.filter {
      it.toString().startsWith(BROWSER_IDENTITY_PROVIDER)
    }

    for (provider in browserProviders) {
      println(provider)
    }
//    omiSdkFacade.oneginiClient.getUserClient().registerUser()
  }

  companion object {
    private const val BROWSER_IDENTITY_PROVIDER = "BrowserIdentityProvider"
  }
}
