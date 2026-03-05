package com.onewelcome.showcaseapp.viewmodel

import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.util.TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER
import com.onewelcome.core.util.TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT
import com.onewelcome.core.util.TestConstants.TEST_PIN
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.FakePinCallback
import com.onewelcome.showcaseapp.feature.pin.PinAuthenticationInputViewModel
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent
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
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject


@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class PinAuthenticationInputViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var pinAuthenticationRequestHandler: PinAuthenticationRequestHandler

  @Inject
  lateinit var mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler

  val pinCallback = FakePinCallback()

  private lateinit var viewModel: PinAuthenticationInputViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = PinAuthenticationInputViewModel(pinAuthenticationRequestHandler)
  }

  @Test
  fun `When authentication is started, Then data should be updated`() {
    val expected = viewModel.uiState.copy(authenticationAttemptCounter = TEST_AUTHENTICATION_ATTEMPT_COUNTER)

    pinAuthenticationRequestHandler.startAuthentication(TEST_USER_PROFILE_1, pinCallback, TEST_AUTHENTICATION_ATTEMPT_COUNTER)

    assertThat(viewModel.uiState).isEqualTo(expected)
  }

  @Test
  fun `When pin authentication is retried, Then data should be updated`() {
    val expected = viewModel.uiState.copy(
      authenticationAttemptCounter = TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT,
      pinValidationError = "Wrong PIN, try again"
    )

    pinAuthenticationRequestHandler.startAuthentication(
      TEST_USER_PROFILE_1,
      pinCallback,
      TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT
    )

    assertThat(viewModel.uiState).isEqualTo(expected)
  }

  @Test
  fun `When pin authentication is finished, Then navigation event should be sent`() {
    val expected = NavigationEvent.PopBackStack

    pinAuthenticationRequestHandler.finishAuthentication()

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(expected)
    }
  }

  @Test
  fun `When Submit event is sent, Then accept authentication request should be triggered`() {
    val spyCreatePinRequestHandler = spy(pinAuthenticationRequestHandler)
    viewModel = PinAuthenticationInputViewModel(spyCreatePinRequestHandler)

    viewModel.onEvent(UiEvent.Submit(TEST_PIN))

    verify(spyCreatePinRequestHandler).pinCallback?.acceptAuthenticationRequest(TEST_PIN)
  }

  @Test
  fun `When Cancel event is sent, Then deny authentication request should be triggered`() {
    val spyCreatePinRequestHandler = spy(pinAuthenticationRequestHandler)
    viewModel = PinAuthenticationInputViewModel(spyCreatePinRequestHandler)

    viewModel.onEvent(UiEvent.Cancel)

    verify(spyCreatePinRequestHandler).pinCallback?.denyAuthenticationRequest()
  }
}
