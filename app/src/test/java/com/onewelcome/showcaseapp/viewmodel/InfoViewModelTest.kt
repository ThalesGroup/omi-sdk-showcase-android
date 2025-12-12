package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetImplicitlyAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsInStatelessSessionUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES_IDS
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_2
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.fakes.PermissionsFacadeFake
import com.onewelcome.showcaseapp.feature.info.InfoViewModel
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
class InfoViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var getImplicitlyAuthenticatedUserProfileUseCase: GetImplicitlyAuthenticatedUserProfileUseCase

  @Inject
  lateinit var isInStatelessSessionUseCase: IsInStatelessSessionUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase

  @Inject
  lateinit var isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase

  @Inject
  lateinit var getAuthenticatorsUseCase: GetAuthenticatorsUseCase

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var permissionsFacadeFake: PermissionsFacadeFake

  @Inject
  lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

  private val userClientMock: UserClient = mock()

  private lateinit var viewModel: InfoViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = InfoViewModel(
      isSdkInitializedUseCase,
      getUserProfilesUseCase,
      getAuthenticatedUserProfileUseCase,
      getImplicitlyAuthenticatedUserProfileUseCase,
      isInStatelessSessionUseCase,
      isUserEnrolledForMobileAuthUseCase,
      isUserEnrolledForMobileAuthWithPushUseCase,
      getAuthenticatorsUseCase,
      permissionsFacadeFake
    )
  }

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then state should be updated`() {
    val expectedState = viewModel.uiState.copy(isSdkInitialized = false, userProfileIds = emptyList(), authenticatedUserProfileId = "")

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are no user profiles, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true, userProfileIds = emptyList(), authenticatedUserProfileId = "")

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are user profiles, When viewmodel is initialized, Then state should be updated`() {
    val expectedAuthenticators = setOf(TestConstants.getPinAuthenticator(), TestConstants.getBiometricAuthenticator(false))
    mockSdkInitialized()
    mockUserClient()
    mockUserProfileIds()
    mockAvailableAuthenticators(expectedAuthenticators)
    mockMobileAuthEnrollmentStatus()
    mockMobileAuthEnrollmentWithPushStatus()
    val expectedState =
      viewModel.uiState.copy(
        isSdkInitialized = true,
        userProfileIds = TEST_USER_PROFILES_IDS,
        authenticatedUserProfileId = "",
        authenticatorsState = listOf(
          InfoViewModel.AuthenticatorsState(TEST_USER_PROFILE_1.profileId, expectedAuthenticators),
          InfoViewModel.AuthenticatorsState(TEST_USER_PROFILE_2.profileId, expectedAuthenticators),
        ),
        mobileAuthenticationEnrollmentState = TEST_USER_PROFILES_IDS.map { InfoViewModel.MobileAuthEnrollmentState(it, true, true) }
      )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and the is no authenticated user profile, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()

    val expectedState = viewModel.uiState.copy(isSdkInitialized = true, userProfileIds = emptyList(), authenticatedUserProfileId = "")

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and the is authenticated user profile, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockAuthenticatedUserProfileId()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = emptyList(),
      authenticatedUserProfileId = TEST_USER_PROFILE_1.profileId
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When getting user profiles failed, Then mobile auth enrollment list should be empty`() {
    mockSdkInitialized()
    mockUserProfilesError()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = emptyList(),
      mobileAuthenticationEnrollmentState = emptyList()
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When mobile auth enrollment check failed, Then mobile auth enrollment list should be empty`() {
    mockSdkInitialized()
    mockUserProfileIds()
    mockMobileAuthEnrollmentStatusError()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = emptyList(),
      mobileAuthenticationEnrollmentState = emptyList()
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When mobile auth enrollment with push check failed, Then mobile auth enrollment list should be empty`() {
    mockSdkInitialized()
    mockUserProfileIds()
    mockMobileAuthEnrollmentStatus()
    mockMobileAuthEnrollmentWithPushStatusError()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = emptyList(),
      mobileAuthenticationEnrollmentState = emptyList()
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given post notification permission is granted, When viewmodel is initialized, Then state should be updated`() {
    permissionsFacadeFake.postNotificationsPermissionGranted = true
    val expectedState = viewModel.uiState.copy(
      isPostNotificationPermissionGranted = true
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given post notification permission is declined, When viewmodel is initialized, Then state should be updated`() {
    permissionsFacadeFake.postNotificationsPermissionGranted = false
    val expectedState = viewModel.uiState.copy(
      isPostNotificationPermissionGranted = false
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given user is in stateless session, When viewmodel is initialized, Then stateless session state should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockNoAuthenticatedUserProfile()
    mockAccessToken()
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      isInStatelessSession = true
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given user is in stateful session, When viewmodel is initialized, Then stateless session state should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockAuthenticatedUserProfileId()
    mockAccessToken()
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      authenticatedUserProfileId = TEST_USER_PROFILE_1.profileId,
      isInStatelessSession = false
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given user has no active session, When viewmodel is initialized, Then stateless session state should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockNoAuthenticatedUserProfile()
    mockNoAccessToken()
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      authenticatedUserProfileId = "",
      isInStatelessSession = false
    )

    viewModel.updateData()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockUserProfileIds() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }

  private fun mockUserProfilesError() {
    whenever(userClientMock.userProfiles).thenThrow(RuntimeException("Some exception"))
  }

  private fun mockAuthenticatedUserProfileId() {
    whenever(userClientMock.authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun mockMobileAuthEnrollmentStatus() {
    whenever(userClientMock.isUserEnrolledForMobileAuth(any())).thenReturn(true)
  }

  private fun mockMobileAuthEnrollmentStatusError() {
    whenever(userClientMock.isUserEnrolledForMobileAuth(any())).thenThrow(RuntimeException("Some excpetion"))
  }

  private fun mockMobileAuthEnrollmentWithPushStatus() {
    whenever(userClientMock.isUserEnrolledForMobileAuthWithPush(any())).thenReturn(true)
  }

  private fun mockMobileAuthEnrollmentWithPushStatusError() {
    whenever(userClientMock.isUserEnrolledForMobileAuthWithPush(any())).thenThrow(RuntimeException("Some excpetion"))
  }

  private fun mockNoAuthenticatedUserProfile() {
    whenever(userClientMock.authenticatedUserProfile).thenReturn(null)
  }

  private fun mockAccessToken() {
    whenever(userClientMock.accessToken).thenReturn("access_token")
  }

  private fun mockNoAccessToken() {
    whenever(userClientMock.accessToken).thenReturn(null)
  }

  private fun mockAvailableAuthenticators(authenticators: Set<OneginiAuthenticator>) {
    whenever(userClientMock.getAllAuthenticators(any())).thenReturn(authenticators)
  }
}
