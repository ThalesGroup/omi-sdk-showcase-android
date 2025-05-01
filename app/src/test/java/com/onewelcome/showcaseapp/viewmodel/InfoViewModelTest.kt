package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onewelcome.core.entity.OmiSdkInitializationSettings
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class InfoViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  private lateinit var viewModel: InfoViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = InfoViewModel(isSdkInitializedUseCase)
  }

  @Test
  fun `should indicate SDK not initialized`() {
    val expectedState = viewModel.uiState.copy(isSdkInitialized = false)

    viewModel.updateStatus()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should indicate SDK is not initialized`() {
    mockSdkInitialized()
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true)

    viewModel.updateStatus()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }
}
