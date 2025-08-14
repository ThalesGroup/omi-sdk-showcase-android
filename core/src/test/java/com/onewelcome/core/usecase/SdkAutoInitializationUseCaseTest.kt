package com.onewelcome.core.usecase

import com.onewelcome.core.omisdk.OmiSdkEngine
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever


@RunWith(MockitoJUnitRunner::class)
class SdkAutoInitializationUseCaseTest {

  @Mock
  private lateinit var dataStore: ShowcaseDataStore

  @Mock
  private lateinit var omiSdkEngine: OmiSdkEngine

  @Mock
  private lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

  private lateinit var useCase: SdkAutoInitializationUseCase

  @Before
  fun setup() {
    useCase = SdkAutoInitializationUseCase(dataStore, omiSdkEngine, browserRegistrationRequestHandler)
  }

  @Test
  fun `Given SDK auto initialization is enabled, When usecase is executed, Then omiSdkEngine should be initialized`() {
    whenever(dataStore.isSdkAutoInitializationEnabled()).thenReturn(flowOf(true))

    runTest {
      useCase.execute()
    }

    verify(omiSdkEngine).initialize(any())
  }

  @Test
  fun `Given SDK auto initialization is disabled, When usecase is executed, Then omiSdkEngine should not be initialized`() {
    whenever(dataStore.isSdkAutoInitializationEnabled()).thenReturn(flowOf(false))

    runTest {
      useCase.execute()
    }

    verifyNoInteractions(omiSdkEngine)
  }
}
