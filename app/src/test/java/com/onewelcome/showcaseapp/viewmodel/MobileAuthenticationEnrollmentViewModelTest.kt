package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthEnrollmentHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthEnrollmentError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentViewModel
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentViewModel.State
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentViewModel.UiEvent
import com.onewelcome.showcaseapp.utils.ThrowableEquals
import com.onewelcome.showcaseapp.utils.withEqualsForThrowable
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class MobileAuthenticationEnrollmentViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase

  @Inject
  lateinit var enrollForMobileAuthenticationUseCase: EnrollForMobileAuthenticationUseCase

  @Inject
  lateinit var omiSdkFacade: OmiSdkFacade

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()
  private val oneginiMobileAuthEnrollmentError = mock<OneginiMobileAuthEnrollmentError>()

  private lateinit var viewModel: MobileAuthenticationEnrollmentViewModel

  @Before
  fun setup() {
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
  fun `Given SDK is not initialized, When EnrollForMobileAuthentication event is sent, Then error result should be returned`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")
    initializeViewModel()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthentication)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(DEFAULT_STATE.copy(enrollmentResult = Err(expectedException)))
  }

  @Test
  fun `Given no user profile is authenticated, When EnrollForMobileAuthentication event is sent, Then error result should be returned`() {
    givenSdkInitializedAndNoUserAuthenticated()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthentication)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          enrollmentResult = Err(oneginiMobileAuthEnrollmentError)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When EnrollForMobileAuthentication event finishes with success, Then success result should be returned`() {
    givenUserProfileIsAuthenticatedAndEnrollmentSuccess()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthentication)

    assertThat(viewModel.uiState)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          enrollmentResult = Ok(Unit)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When EnrollForMobileAuthentication event finished with error, Then error result should be returned`() {
    givenUserProfileIsAuthenticatedAndEnrollmentFailed()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthentication)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          enrollmentResult = Err(oneginiMobileAuthEnrollmentError)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When EnrollForMobileAuthentication event is sent, Then loading state should be true`() {
    givenUserProfileIsAuthenticated()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthentication)

    assertThat(viewModel.uiState)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          isLoading = true
        )
      )
  }

  private fun givenSdkInitializedAndNoUserAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(false)
    initializeViewModel()
    mockMobileAuthEnrollmentError()
  }

  private fun givenSdkInitializedAndUserAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
  }

  private fun givenUserProfileIsAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
  }

  private fun givenUserProfileIsAuthenticatedAndEnrollmentSuccess() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
    mockMobileAuthEnrollmentSuccess()
  }

  private fun givenUserProfileIsAuthenticatedAndEnrollmentFailed() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
    mockMobileAuthEnrollmentError()
  }

  private fun initializeViewModel() {
    viewModel = MobileAuthenticationEnrollmentViewModel(
      isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase,
      isUserEnrolledForMobileAuthUseCase,
      enrollForMobileAuthenticationUseCase
    )
  }

  private fun mockSdkInitialized() {
    omiSdkFacade.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
  }

  private fun mockAuthenticatedUserProfile(isAuthenticated: Boolean) {
    val userProfile = if (isAuthenticated) AUTHENTICATED_USER_PROFILE else null
    whenever(userClientMock.authenticatedUserProfile).thenReturn(userProfile)
  }

  private fun mockMobileAuthEnrollmentError() {
    whenever(userClientMock.enrollUserForMobileAuth(any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthEnrollmentHandler>(0).onError(oneginiMobileAuthEnrollmentError)
    }
  }

  private fun mockMobileAuthEnrollmentSuccess() {
    whenever(userClientMock.enrollUserForMobileAuth(any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthEnrollmentHandler>(0).onSuccess()
    }
  }

  companion object {
    private val DEFAULT_STATE = State(
      isSdkInitialized = false,
      authenticatedUserProfile = null,
      enrollmentResult = null,
      isLoading = false
    )
    private val AUTHENTICATED_USER_PROFILE = UserProfile("QWERTY")
  }
}
