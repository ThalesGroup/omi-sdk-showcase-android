package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiImplicitAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiImplicitTokenRequestError
import com.onewelcome.core.usecase.GetImplicitlyAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.ImplicitAuthenticationUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_SELECTED_SCOPES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel.UiEvent.StartImplicitAuthentication
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel.UiEvent.UpdateSelectedUserProfile
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel.UiEvent.UpdateSelectedScopes
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
class ImplicitAuthenticationViewModelTest {
  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var implicitAuthenticationUseCase: ImplicitAuthenticationUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var getImplicitlyAuthenticatedUserProfileUseCase: GetImplicitlyAuthenticatedUserProfileUseCase
  private val userClientMock: UserClient = mock()
  private val mockOneginiImplicitTokenRequestError: OneginiImplicitTokenRequestError = mock()
  lateinit var viewModel: ImplicitAuthenticationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = ImplicitAuthenticationViewModel(
      isSdkInitializedUseCase,
      getUserProfilesUseCase,
      implicitAuthenticationUseCase,
      getImplicitlyAuthenticatedUserProfileUseCase
    )
  }

  @Test
  fun `Given sdk is not initialized, Then default state should be returned`() {
    val expectedState = viewModel.uiState.copy(
      result = null,
      isSdkInitialized = false,
      selectedScopes = emptyList(),
      userProfiles = emptySet(),
      selectedUserProfile = null,
      isAuthenticateButtonEnabled = false
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When ViewModel is initialized, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true)

    mockSdkInitialized()

    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and user profiles are present, When ViewModel is initialized, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true
    )

    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, registered and authenticated user profiles are present, When ViewModel is initialized, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true,
      implicitlyAuthenticatedUserProfile = TEST_USER_PROFILES.first()
    )

    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockImplicitlyAuthenticatedProfile()

    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected scopes event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedScopes = TEST_SELECTED_SCOPES)

    viewModel.onEvent(UpdateSelectedScopes(TEST_SELECTED_SCOPES))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given user profile is selected, When start implicit authentication event is sent and finished successfully, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(
      result = Ok(TEST_USER_PROFILE_1),
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true,
      implicitlyAuthenticatedUserProfile = TEST_USER_PROFILES.first()
    )

    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockSuccessfulImplicitAuthentication()

    initializeViewModel()

    viewModel.onEvent(StartImplicitAuthentication)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given user profile is selected, When start implicit authentication event is sent and finishes with error, Then error should be returned`() {
    val expectedState = viewModel.uiState.copy(
      result = Err(mockOneginiImplicitTokenRequestError),
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true,
    )

    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockUnsuccessfulImplicitAuthentication()

    initializeViewModel()

    viewModel.onEvent(StartImplicitAuthentication)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected user profile event is sent, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(selectedUserProfile = TEST_USER_PROFILE_1)

    viewModel.onEvent(UpdateSelectedUserProfile(TEST_USER_PROFILE_1))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun initializeViewModel() {
    viewModel = ImplicitAuthenticationViewModel(
      isSdkInitializedUseCase,
      getUserProfilesUseCase,
      implicitAuthenticationUseCase,
      getImplicitlyAuthenticatedUserProfileUseCase
    )
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }

  private fun mockImplicitlyAuthenticatedProfile() {
    whenever(userClientMock.implicitlyAuthenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun mockSuccessfulImplicitAuthentication() {
    whenever(userClientMock.authenticateUserImplicitly(any(), any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiImplicitAuthenticationHandler>(2).onSuccess(TEST_USER_PROFILE_1)
      }
  }

  private fun mockUnsuccessfulImplicitAuthentication() {
    whenever(userClientMock.authenticateUserImplicitly(any(), any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiImplicitAuthenticationHandler>(2).onError(mockOneginiImplicitTokenRequestError)
      }
  }
}
