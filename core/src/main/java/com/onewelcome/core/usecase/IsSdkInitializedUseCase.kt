package com.onewelcome.core.usecase

import com.github.michaelbull.result.runCatching
import com.onewelcome.core.facade.OmiSdkFacade
import javax.inject.Inject

class IsSdkInitializedUseCase @Inject constructor(private val omiSdkFacade: OmiSdkFacade) {
  fun execute(): Boolean = runCatching { omiSdkFacade.oneginiClient }.isOk
}
