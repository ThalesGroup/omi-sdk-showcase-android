package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthWithOtpHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthWithOtpError
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.MobileAuthWithOtpRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithOtpUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiState
import com.onewelcome.showcaseapp.utils.withEqualsForThrowable
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class MobileAuthenticationWithOtpViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)


  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase

  @Inject
  lateinit var authenticateWithOtpUseCase: AuthenticateWithOtpUseCase

  @Inject
  lateinit var mobileAuthWithOtpRequestHandler: MobileAuthWithOtpRequestHandler

  @Inject
  lateinit var omiSdkFacade: OmiSdkFacade

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()
  private val oneginiMobileAuthWithOtpError = mock<OneginiMobileAuthWithOtpError>()

  private lateinit var viewModel: MobileAuthenticationWithOtpViewModel


  @Before
  fun setUp() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  @Test
  fun `Given SDK is not initialized, When view model is initialized, Then default state should be returned`() {
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE)
  }

  @Test
  fun `Given SDK is initialized and no user profile in authenticated, When view model is initialized, Then state should be updated`() {
    givenSdkInitializedAndNoUserAuthenticated()

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isSdkInitialized = true
      )
    )
  }

  @Test
  fun `Given SDK is initialized and user profile is authenticated, When view model is initialized, Then state should be updated`() {
    givenSdkInitializedAndUserAuthenticated()

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isSdkInitialized = true,
        authenticatedUserProfile = AUTHENTICATED_USER_PROFILE
      )
    )
  }

  @Test
  fun `Given SDK is not initialized, When AuthenticateWithOtp event is sent, Then error result should be returned`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")
    initializeViewModel()

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(DEFAULT_STATE.copy(authenticationResult = Err(expectedException)))
  }

  @Test
  fun `Given no user profile is authenticated, When AuthenticateWithOtp event is sent, Then error result should be returned`() {
    givenSdkInitializedAndNoUserAuthenticated()

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticationResult = Err(oneginiMobileAuthWithOtpError)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When AuthenticateWithOtp event finishes with success, Then success result should be returned`() {
    givenUserProfileIsAuthenticatedAndAuthenticationSuccess()

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          authenticationResult = Ok(Unit)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When AuthenticateWithOtp event finished with error, Then error result should be returned`() {
    givenUserProfileIsAuthenticatedAndAuthenticationFailed()

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          authenticationResult = Err(oneginiMobileAuthWithOtpError)
        )
      )
  }

  @Test
  fun `Given otp value, When UpdateOtpValue event is sent, Then otp value should update`() {
    initializeViewModel()
    val newOtp = "123456"

    viewModel.onEvent(UiEvent.UpdateOtpValue(newOtp))

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        otp = newOtp
      )
    )
  }

  @Test
  fun `Given authentication started, When mobile auth request is received, Then update state with request`() {
    givenSdkInitializedAndUserAuthenticated()
    val expectedMobileAuthRequest = OneginiMobileAuthenticationRequest(
      message = "message",
      type = "otp",
      userProfile = AUTHENTICATED_USER_PROFILE,
      transactionId = "transaction_id",
      signingData = null
    )
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any()))
      .thenAnswer { mobileAuthWithOtpRequestHandler.startAuthentication(expectedMobileAuthRequest, mock()) }

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          isLoading = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          mobileAuthRequestToHandle = expectedMobileAuthRequest
        )
      )
  }

  @Test
  fun `Given mobile auth request received, When AcceptAuthRequest event is sent, Then mobile auth callback should be accepted`() = runTest {
    val awaitAuthenticationStart = Job()
    val oneginiCallback = mock<OneginiAcceptDenyCallback>()
    givenSdkInitializedAndUserAuthenticated()
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any()))
      .thenAnswer { invocation ->
        mobileAuthWithOtpRequestHandler.startAuthentication(mock(), oneginiCallback)
        awaitAuthenticationStart.complete()
      }

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)
    awaitAuthenticationStart.join()
    viewModel.onEvent(UiEvent.AcceptAuthRequest)


    verify(oneginiCallback).acceptAuthenticationRequest()
  }

  @Test
  fun `Given mobile auth request received, When RejectAuthRequest event is sent, Then mobile auth callback should be denied`() = runTest {
    val awaitAuthenticationStart = Job()
    val oneginiCallback = mock<OneginiAcceptDenyCallback>()
    givenSdkInitializedAndUserAuthenticated()
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any()))
      .thenAnswer { invocation ->
        mobileAuthWithOtpRequestHandler.startAuthentication(mock(), oneginiCallback)
        awaitAuthenticationStart.complete()
      }

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)
    awaitAuthenticationStart.join()
    viewModel.onEvent(UiEvent.RejectAuthRequest)


    verify(oneginiCallback).denyAuthenticationRequest()
  }

  @Test
  fun `Given mobile auth request received, When AuthRequestHandled event is sent, Then request should be removed from state`() = runTest {
    val awaitAuthenticationStart = Job()
    givenSdkInitializedAndUserAuthenticated()
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any()))
      .thenAnswer { invocation ->
        mobileAuthWithOtpRequestHandler.startAuthentication(mock(), mock())
        awaitAuthenticationStart.complete()
      }

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)
    awaitAuthenticationStart.join()
    assertThat(viewModel.uiState.mobileAuthRequestToHandle).isNotNull

    viewModel.onEvent(UiEvent.AuthRequestHandled)

    assertThat(viewModel.uiState.mobileAuthRequestToHandle).isNull()
  }

  @Test
  fun `Given mobile auth flow, When flow is handled, Then loading state should change`() = runTest {
    lateinit var invocationOnAuthHandler: InvocationOnMock
    val awaitAuthenticationStart = Job()
    givenSdkInitializedAndUserAuthenticated()
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any()))
      .thenAnswer { invocation ->
        invocationOnAuthHandler = invocation
        awaitAuthenticationStart.complete()
      }

    assertThat(viewModel.uiState.isLoading).isFalse

    viewModel.onEvent(UiEvent.AuthenticateWithOtp)

    assertThat(viewModel.uiState.isLoading).isTrue

    awaitAuthenticationStart.join()
    invocationOnAuthHandler.getArgument<OneginiMobileAuthWithOtpHandler>(1).onError(oneginiMobileAuthWithOtpError)

    assertThat(viewModel.uiState.isLoading).isFalse
  }

  private fun givenSdkInitializedAndNoUserAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(false)
    initializeViewModel()
    mockMobileAuthWithOtpError()
  }

  private fun givenSdkInitializedAndUserAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
  }

  private fun givenUserProfileIsAuthenticatedAndAuthenticationSuccess() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
    mockMobileAuthWithOtpSuccess()
  }

  private fun givenUserProfileIsAuthenticatedAndAuthenticationFailed() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
    mockMobileAuthWithOtpError()
  }

  private fun initializeViewModel() {
    viewModel = MobileAuthenticationWithOtpViewModel(
      isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase,
      isUserEnrolledForMobileAuthUseCase,
      authenticateWithOtpUseCase,
      mobileAuthWithOtpRequestHandler
    )
  }

  private fun mockSdkInitialized() {
    omiSdkFacade.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
  }

  private fun mockAuthenticatedUserProfile(isAuthenticated: Boolean) {
    val userProfile = if (isAuthenticated) AUTHENTICATED_USER_PROFILE else null
    whenever(userClientMock.authenticatedUserProfile).thenReturn(userProfile)
  }

  private fun mockMobileAuthWithOtpSuccess() {
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthWithOtpHandler>(1).onSuccess()
    }
  }

  private fun mockMobileAuthWithOtpError() {
    whenever(userClientMock.handleMobileAuthWithOtp(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthWithOtpHandler>(1).onError(oneginiMobileAuthWithOtpError)
    }
  }

  companion object {
    private val DEFAULT_STATE = UiState(
      isSdkInitialized = false,
      authenticatedUserProfile = null,
      isUserEnrolledForMobileAuth = false,
      authenticationResult = null,
      isLoading = false,
      otp = "",
      mobileAuthRequestToHandle = null
    )
    private val AUTHENTICATED_USER_PROFILE = UserProfile("QWERTY")
  }
}
