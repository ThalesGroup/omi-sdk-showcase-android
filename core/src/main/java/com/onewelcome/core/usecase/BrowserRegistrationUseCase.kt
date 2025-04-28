package com.onewelcome.core.usecase

import com.onewelcome.core.facade.OmiSdkFacade
import javax.inject.Inject

class BrowserRegistrationUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  fun execute() {
//    omiSdkFacade.oneginiClient.getUserClient().registerUser()
  }
}
