package com.onewelcome.showcaseapp.usecase

import com.github.michaelbull.result.runCatching
import com.onewelcome.showcaseapp.core.facade.OmiSdkFacade
import javax.inject.Inject

class IsSdkInitializedUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {

  fun execute(): Boolean {
    return runCatching { omiSdkFacade.oneginiClient }
      .isOk
  }
}
