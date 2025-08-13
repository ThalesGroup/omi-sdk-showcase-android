package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthWithPushEnrollmentHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthWithPushEnrollmentError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationWithPushUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import com.onewelcome.showcaseapp.fakes.FirebaseMessagingFacadeFake
import com.onewelcome.showcaseapp.fakes.PermissionsFacadeFake
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.State
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class MobileAuthenticationWithPushEnrollmentViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkFacade: OmiSdkFacade

  @Inject
  lateinit var firebaseMessagingFacadeFake: FirebaseMessagingFacadeFake

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase

  @Inject
  lateinit var enrollForMobileAuthenticationWithPushUseCase: EnrollForMobileAuthenticationWithPushUseCase

  @Inject
  lateinit var permissionsFacadeFake: PermissionsFacadeFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()
  private val oneginiMobileAuthWithPushEnrollmentError = mock<OneginiMobileAuthWithPushEnrollmentError>()

  private lateinit var viewModel: MobileAuthenticationWithPushEnrollmentViewModel

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
  fun `Given firebase token fetch is not available, When EnrollForMobileAuthenticationWithPush event is sent, Then error result should be returned`() {
    val expectedException = Exception("Firebase error")
    givenFirebaseTokenIsNotAvailable()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(DEFAULT_STATE.copy(enrollmentResult = Err(expectedException)))
  }

  @Test
  fun `Given SDK is not initialized, When EnrollForMobileAuthenticationWithPush event is sent, Then error result should be returned`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")
    initializeViewModel()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(DEFAULT_STATE.copy(enrollmentResult = Err(expectedException)))
  }

  @Test
  fun `Given no user profile is authenticated, When EnrollForMobileAuthenticationWithPush event is sent, Then error result should be returned`() {
    givenSdkInitializedAndNoUserAuthenticated()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          enrollmentResult = Err(oneginiMobileAuthWithPushEnrollmentError)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When EnrollForMobileAuthenticationWithPush event finishes with success, Then success result should be returned`() {
    givenUserProfileIsAuthenticatedAndEnrollmentSuccess()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

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
  fun `Given user profile is authenticated, When EnrollForMobileAuthenticationWithPush event finished with error, Then error result should be returned`() {
    givenUserProfileIsAuthenticatedAndEnrollmentFailed()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          enrollmentResult = Err(oneginiMobileAuthWithPushEnrollmentError)
        )
      )
  }

  @Test
  fun `Given user profile is authenticated, When EnrollForMobileAuthenticationWithPush event is sent, Then loading state should be true`() {
    givenUserProfileIsAuthenticated()

    viewModel.onEvent(UiEvent.EnrollForMobileAuthenticationWithPush)

    assertThat(viewModel.uiState)
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          authenticatedUserProfile = AUTHENTICATED_USER_PROFILE,
          isLoading = true
        )
      )
  }

  @Test
  fun `Given post notifications permission granted, When PostNotificationsPermissionClicked event is sent, Then should show settings dialog`() {
    permissionsFacadeFake.postNotificationsPermissionGranted = true
    initializeViewModel()

    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(false))

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE.copy(isPostNotificationPermissionGranted = true, showSettingsDialog = true))
  }

  @Test
  fun `Given post notifications permission not granted, When PostNotificationsPermissionClicked event is sent, Then should request permission`() {
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()

    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(true))

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isPostNotificationPermissionGranted = false,
        requestPostNotificationsPermission = true
      )
    )
  }

  @Test
  fun `Given post notifications permission was requested, When RequestPostNotificationsPermissionResult GRANTED event is sent, Then should update the state`() {
    val expectedStateAfterPermissionRequest = DEFAULT_STATE.copy(
      isPostNotificationPermissionGranted = false,
      requestPostNotificationsPermission = true
    )
    val expectedFinalState = expectedStateAfterPermissionRequest.copy(
      isPostNotificationPermissionGranted = true,
      requestPostNotificationsPermission = false
    )
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()
    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(true))
    assertThat(viewModel.uiState).isEqualTo(expectedStateAfterPermissionRequest)

    viewModel.onEvent(UiEvent.RequestPostNotificationsPermissionResult.GRANTED)

    assertThat(viewModel.uiState).isEqualTo(expectedFinalState)
  }

  @Test
  fun `Given post notifications permission was requested, When RequestPostNotificationsPermissionResult DECLINED event is sent, Then should update the state`() {
    val expectedStateAfterPermissionRequest = DEFAULT_STATE.copy(
      isPostNotificationPermissionGranted = false,
      requestPostNotificationsPermission = true
    )
    val expectedFinalState = expectedStateAfterPermissionRequest.copy(
      isPostNotificationPermissionGranted = false,
      requestPostNotificationsPermission = false
    )
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()
    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(true))
    assertThat(viewModel.uiState).isEqualTo(expectedStateAfterPermissionRequest)

    viewModel.onEvent(UiEvent.RequestPostNotificationsPermissionResult.DECLINED)

    assertThat(viewModel.uiState).isEqualTo(expectedFinalState)
  }

  @Test
  fun `Given post notifications permission was requested, When RequestPostNotificationsPermissionResult PERMANENTLY_DECLINED event is sent, Then should update the state`() {
    val expectedStateAfterPermissionRequest = DEFAULT_STATE.copy(
      isPostNotificationPermissionGranted = false,
      requestPostNotificationsPermission = true
    )
    val expectedFinalState = expectedStateAfterPermissionRequest.copy(
      isPostNotificationPermissionGranted = false,
      requestPostNotificationsPermission = false,
      showSettingsDialog = true
    )
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()
    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(true))
    assertThat(viewModel.uiState).isEqualTo(expectedStateAfterPermissionRequest)

    viewModel.onEvent(UiEvent.RequestPostNotificationsPermissionResult.PERMANENTLY_DECLINED)

    assertThat(viewModel.uiState).isEqualTo(expectedFinalState)
  }

  @Test
  fun `Given settings dialog is shown, When DismissSettingsDialog event is sent, Then should not show settings dialog anymore`() {
    val expectedInitialState = DEFAULT_STATE.copy(showSettingsDialog = true)
    val expectedFinalState = expectedInitialState.copy(showSettingsDialog = false)
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()
    viewModel.onEvent(UiEvent.PostNotificationsPermissionClicked(false))
    assertThat(viewModel.uiState).isEqualTo(expectedInitialState)

    viewModel.onEvent(UiEvent.DismissSettingsDialog)

    assertThat(viewModel.uiState).isEqualTo(expectedFinalState)
  }

  @Test
  fun `Given view model is initialized, When UpdatePostNotificationsPermissionState event is sent, Then should check for post notifications permission`() {
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    initializeViewModel()
    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE)

    permissionsFacadeFake.postNotificationsPermissionGranted = true
    viewModel.onEvent(UiEvent.UpdatePostNotificationsPermissionState)

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE.copy(isPostNotificationPermissionGranted = true))
  }

  private fun givenSdkInitializedAndNoUserAuthenticated() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(false)
    initializeViewModel()
    mockMobileAuthWithPushEnrollmentError()
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
    mockMobileAuthWithPushEnrollmentSuccess()
  }

  private fun givenUserProfileIsAuthenticatedAndEnrollmentFailed() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile(true)
    initializeViewModel()
    mockMobileAuthWithPushEnrollmentError()
  }

  private fun givenFirebaseTokenIsNotAvailable() {
    mockFirebaseTokenFetchFailed()
    initializeViewModel()
  }

  private fun initializeViewModel() {
    viewModel = MobileAuthenticationWithPushEnrollmentViewModel(
      isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase,
      isUserEnrolledForMobileAuthUseCase,
      isUserEnrolledForMobileAuthWithPushUseCase,
      enrollForMobileAuthenticationWithPushUseCase,
      permissionsFacadeFake
    )
  }

  private fun mockSdkInitialized() {
    omiSdkFacade.initialize(OmiSdkInitializationSettings(true, null, null, null))
  }

  private fun mockAuthenticatedUserProfile(isAuthenticated: Boolean) {
    val userProfile = if (isAuthenticated) AUTHENTICATED_USER_PROFILE else null
    whenever(userClientMock.authenticatedUserProfile).thenReturn(userProfile)
  }

  private fun mockMobileAuthWithPushEnrollmentError() {
    whenever(userClientMock.enrollUserForMobileAuthWithPush(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthWithPushEnrollmentHandler>(1).onError(oneginiMobileAuthWithPushEnrollmentError)
    }
  }

  private fun mockMobileAuthWithPushEnrollmentSuccess() {
    whenever(userClientMock.enrollUserForMobileAuthWithPush(eq("token"), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthWithPushEnrollmentHandler>(1).onSuccess()
    }
  }

  private fun mockFirebaseTokenFetchFailed() {
    firebaseMessagingFacadeFake.resultFake = Err(Exception("Firebase error"))
  }

  companion object {
    private val DEFAULT_STATE = State(
      isSdkInitialized = false,
      authenticatedUserProfile = null,
      isUserEnrolledForMobileAuth = false,
      isUserEnrolledForMobileAuthWithPush = false,
      isPostNotificationPermissionGranted = false,
      enrollmentResult = null,
      isLoading = false,
      requestPostNotificationsPermission = false,
      showSettingsDialog = false
    )
    private val AUTHENTICATED_USER_PROFILE = UserProfile("QWERTY")
  }
}
