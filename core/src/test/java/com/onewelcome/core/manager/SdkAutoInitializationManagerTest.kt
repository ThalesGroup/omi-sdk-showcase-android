package com.onewelcome.core.manager

import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
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
class SdkAutoInitializationManagerTest {

  @Mock
  private lateinit var sdkInitializationUseCase: OmiSdkInitializationUseCase

  @Mock
  private lateinit var dataStore: ShowcaseDataStore

  private lateinit var manager: SdkAutoInitializationManager

  @Before
  fun setup() {
    manager = SdkAutoInitializationManager(sdkInitializationUseCase, dataStore)
  }

  @Test
  fun `Given SDK auto initialization is enabled, When manager is executed, Then omi sdk should be initialized`() {
    whenever(dataStore.isSdkAutoInitializationEnabled()).thenReturn(flowOf(true))

    runTest {
      manager.execute()
      manager.deferredResult?.await()
      verify(sdkInitializationUseCase).initialize(any())
    }
  }

  @Test
  fun `Given SDK auto initialization is disabled, When manager is executed, Then omi sdk should not be initialized`() {
    whenever(dataStore.isSdkAutoInitializationEnabled()).thenReturn(flowOf(false))

    runTest {
      manager.execute()
    }

    verifyNoInteractions(sdkInitializationUseCase)
  }
}
