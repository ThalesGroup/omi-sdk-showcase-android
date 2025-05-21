package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.handlers.error.OneginiPinValidationError
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.usecase.PinUseCase
import com.onewelcome.core.util.Constants.TEST_PIN
import com.onewelcome.core.util.Constants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.pin.NavigationEvent
import com.onewelcome.showcaseapp.feature.pin.PinViewModel
import com.onewelcome.showcaseapp.feature.pin.UiEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class PinViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var pinUseCase: PinUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var createPinRequestHandler: CreatePinRequestHandler

  private val mockOneginiPinValidationError: OneginiPinValidationError = mock()

  val pinCallback = FakePinCallback()

  private lateinit var viewModel: PinViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = PinViewModel(pinUseCase)
  }

  @Test
  fun `When viewmodel is initialized Then state should be updated`() {
    val expected = viewModel.uiState.copy(maxPinLength = 5)

    createPinRequestHandler.startPinCreation(TEST_USER_PROFILE_1, pinCallback, 5)
    viewModel = PinViewModel(pinUseCase)

    assertThat(viewModel.uiState).isEqualTo(expected)
  }

  @Test
  fun `When invalid pin is provided Then state should be updated`() {
    val pinValidationErrorMessage = "pin validation error"
    whenever(mockOneginiPinValidationError.message).thenReturn(pinValidationErrorMessage)
    val expected = viewModel.uiState.copy(pinValidationError = pinValidationErrorMessage)

    createPinRequestHandler.onNextPinCreationAttempt(mockOneginiPinValidationError)

    assertThat(viewModel.uiState).isEqualTo(expected)
  }

  @Test
  fun `When Pin flow is finished, Then navigation event should be sent`() {
    val expected = NavigationEvent.PopBackStack

    createPinRequestHandler.finishPinCreation()

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(expected)
    }
  }

  @Test
  fun `When OnPinProvided event is sent, Then useCase should trigger`() {
    val spyPinUseCase = spy(pinUseCase)
    viewModel = PinViewModel(spyPinUseCase)

    viewModel.onEvent(UiEvent.OnPinProvided(TEST_PIN))

    verify(spyPinUseCase).onPinProvided(TEST_PIN)
  }

  @Test
  fun `When CancelPinFlow event is sent, Then useCase should trigger`() {
    val spyPinUseCase = spy(pinUseCase)
    viewModel = PinViewModel(spyPinUseCase)

    viewModel.onEvent(UiEvent.CancelPinFlow)

    verify(spyPinUseCase).cancelPinFlow()
  }
}

class FakePinCallback : OneginiPinCallback {
  override fun acceptAuthenticationRequest(pin: CharArray) {
    //no-op
  }

  override fun denyAuthenticationRequest() {
    //no-op
  }
}
