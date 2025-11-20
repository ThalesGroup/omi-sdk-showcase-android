package com.onewelcome.showcaseapp.viewmodel

import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.util.TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER
import com.onewelcome.core.util.TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT
import com.onewelcome.core.util.TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST
import com.onewelcome.showcaseapp.fakes.FakePinCallback
import com.onewelcome.showcaseapp.feature.pin.PinViewModel
import com.onewelcome.showcaseapp.feature.pin.PushWithPinConfirmationInputViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class PushWithPinConfirmationInputViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler

  val pinCallback = FakePinCallback()

  val defaultState = PinViewModel.State(0, "", null)

  private lateinit var viewModel: PushWithPinConfirmationInputViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = PushWithPinConfirmationInputViewModel(mobileAuthWithPushPinRequestHandler)
  }

  @Test
  fun `When viewmodel is initialized, Then state should have default values`() {
    assertThat(viewModel.uiState).isEqualTo(defaultState)
  }

  @Test
  fun `When pin confirmation has started, Then state should be updated`() {
    mobileAuthWithPushPinRequestHandler.startAuthentication(
      TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      pinCallback,
      TEST_AUTHENTICATION_ATTEMPT_COUNTER,
      null
    )

    assertThat(viewModel.uiState).isEqualTo(defaultState.copy(authenticationAttemptCounter = TEST_AUTHENTICATION_ATTEMPT_COUNTER))
  }

  @Test
  fun `When pin confirmation is retried, Then data should be updated`() {
    val expected = defaultState.copy(
      authenticationAttemptCounter = TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT,
      pinValidationError = "Wrong PIN, try again"
    )

    mobileAuthWithPushPinRequestHandler.startAuthentication(
      TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      pinCallback,
      TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT,
      null
    )

    assertThat(viewModel.uiState).isEqualTo(expected)
  }
}
