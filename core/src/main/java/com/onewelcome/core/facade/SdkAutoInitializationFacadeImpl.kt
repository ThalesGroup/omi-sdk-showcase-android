package com.onewelcome.core.facade

import com.onewelcome.core.usecase.SdkAutoInitializationUseCase
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SdkAutoInitializationFacadeImpl @Inject constructor(
  private val sdkAutoInitializationUseCase: SdkAutoInitializationUseCase,
  private val dataStore: ShowcaseDataStore
) : SdkAutoInitializationFacade {
  override suspend fun execute() {
    if (dataStore.isSdkAutoInitializationEnabled().first()) {
      sdkAutoInitializationUseCase.execute()
    }
  }
}
