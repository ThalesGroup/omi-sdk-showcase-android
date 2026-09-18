package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onewelcome.core.omisdk.actions.QrCodeRegistrationAction
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel.UiEvent.CancelRegistration
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel.UiEvent.ProceedWithRegistration
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationOtpViewModel.UiEvent.UpdateOtpValue
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
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class OneStepRegistrationOtpViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var qrCodeRegistrationAction: QrCodeRegistrationAction

  private lateinit var viewModel: OneStepRegistrationOtpViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
    viewModel = OneStepRegistrationOtpViewModel(qrCodeRegistrationAction)
  }

  // ─── Initialization ───────────────────────────────────────────────────────

  @Test
  fun `When viewmodel is initialized, Then default state should have empty otp`() {
    val expectedState = OneStepRegistrationOtpViewModel.State(otp = "")

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  // ─── UI Events ────────────────────────────────────────────────────────────

  @Test
  fun `When UpdateOtpValue event is sent, Then state should be updated with new otp value`() {
    val newOtp = "123456"
    val expectedState = OneStepRegistrationOtpViewModel.State(otp = newOtp)

    viewModel.onEvent(UpdateOtpValue(newOtp))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When UpdateOtpValue event is sent multiple times, Then state should reflect the latest otp value`() {
    viewModel.onEvent(UpdateOtpValue("111111"))
    viewModel.onEvent(UpdateOtpValue("222222"))
    viewModel.onEvent(UpdateOtpValue("333333"))

    assertThat(viewModel.uiState.otp).isEqualTo("333333")
  }

  @Test
  fun `When UpdateOtpValue event is sent with empty string, Then state should have empty otp`() {
    viewModel.onEvent(UpdateOtpValue("123456"))
    viewModel.onEvent(UpdateOtpValue(""))

    assertThat(viewModel.uiState.otp).isEmpty()
  }

  // ─── Proceed With Registration ────────────────────────────────────────────

  @Test
  fun `Given a callback is set, When ProceedWithRegistration event is sent, Then callback returnSuccess should be called with otp`() = runTest {
    val otp = "654321"
    val mockCallback: OneginiCustomRegistrationCallback = mock()
    qrCodeRegistrationAction.finishRegistration(mockCallback, null)

    viewModel.onEvent(UpdateOtpValue(otp))
    viewModel.onEvent(ProceedWithRegistration)

    verify(mockCallback).returnSuccess(otp)
  }

  @Test
  fun `Given no callback is set, When ProceedWithRegistration event is sent, Then no exception should be thrown`() = runTest {
    qrCodeRegistrationAction.reset()

    viewModel.onEvent(UpdateOtpValue("123456"))
    viewModel.onEvent(ProceedWithRegistration)

    // No exception expected - callback is null-safe
  }

  @Test
  fun `When ProceedWithRegistration event is sent, Then NavigateBack navigation event should be emitted`() = runTest {
    val expected = NavigationEvent.NavigateBack

    viewModel.onEvent(ProceedWithRegistration)

    assertThat(viewModel.navigationEvents.first()).isEqualTo(expected)
  }

  // ─── Cancel Registration ──────────────────────────────────────────────────

  @Test
  fun `Given a callback is set, When CancelRegistration event is sent, Then callback returnError should be called`() = runTest {
    val mockCallback: OneginiCustomRegistrationCallback = mock()
    qrCodeRegistrationAction.finishRegistration(mockCallback, null)

    viewModel.onEvent(CancelRegistration)

    verify(mockCallback).returnError(org.mockito.kotlin.any())
  }

  @Test
  fun `Given no callback is set, When CancelRegistration event is sent, Then no exception should be thrown`() = runTest {
    qrCodeRegistrationAction.reset()

    viewModel.onEvent(CancelRegistration)

    // No exception expected - callback is null-safe
  }

  @Test
  fun `When CancelRegistration event is sent, Then NavigateBack navigation event should be emitted`() = runTest {
    val expected = NavigationEvent.NavigateBack

    viewModel.onEvent(CancelRegistration)

    assertThat(viewModel.navigationEvents.first()).isEqualTo(expected)
  }
}
