package com.onewelcome.core.usecase

import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class IsCustomAuthHandlerRegisteredUseCase @Inject constructor(
    private val omiSdkFacade: OmiSdkFacade
) {
    fun execute(): Boolean = omiSdkFacade.isCustomAuthHandlerRegistered
}
