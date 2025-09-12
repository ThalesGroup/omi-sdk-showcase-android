package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_CUSTOM_INFO
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class SharedPushViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler

  @Inject
  lateinit var authenticateWithPushUseCase: AuthenticateWithPushUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  lateinit var viewModel: SharedPushViewModel

  private val userClientMock: UserClient = mock()

  private val mockOneginiMobileAuthenticationError: OneginiMobileAuthenticationError = mock()

  private val pushRequest = OneginiMobileAuthWithPushRequest("transactionId", "message", "userProfileId")

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = SharedPushViewModel(authenticateWithPushUseCase, mobileAuthWithPushRequestHandler)
  }

  //accept
  //reject

  @Test
  fun `When viewmodel is initialized, Then default state should be returned`() {
    val expectedState = INITIAL_STATE

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When new push is sent, Then state should be updated`() {
    mockSdkInitialized()

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE.copy(pushRequest = pushRequest))
  }

  @Test
  fun `When new push is sent and authentication is successful and returns null, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onSuccess(null)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(viewModel.uiState.copy(pushRequest = pushRequest, result = Ok(null)))
  }

  @Test
  fun `When new push is sent and authentication is successful and returns Custom Info, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onSuccess(TEST_CUSTOM_INFO)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(viewModel.uiState.copy(pushRequest = pushRequest, result = Ok(TEST_CUSTOM_INFO)))
  }

  @Test
  fun `When new push is sent and authentication failed, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onError(mockOneginiMobileAuthenticationError)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(
      viewModel.uiState.copy(
        pushRequest = pushRequest,
        result = Err(mockOneginiMobileAuthenticationError)
      )
    )
  }

  @Test
  fun `When new push is sent and Accept event is sent, Then state should  be updated`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onNewPush(pushRequest)
    viewModel.onEvent(SharedPushViewModel.UiEvent.Accept)

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE.copy(pushRequest = pushRequest, result = Ok(null)))
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  companion object {
    private val INITIAL_STATE = SharedPushViewModel.UiState(pushRequest = null, result = null)
  }
}
