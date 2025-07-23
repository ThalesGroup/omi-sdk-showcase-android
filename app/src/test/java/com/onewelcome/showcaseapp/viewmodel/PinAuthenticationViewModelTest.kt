package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.PinAuthenticationUseCase
import com.onewelcome.core.util.TestConstants.TEST_SELECTED_IDENTITY_PROVIDER
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.UiEvent.LoadData
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.UiEvent.UpdateSelectedUserProfile
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent.UpdateSelectedIdentityProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class PinAuthenticationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase

  @Inject
  lateinit var pinAuthenticationUseCase: PinAuthenticationUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var pinAuthenticationRequestHandler: PinAuthenticationRequestHandler

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  private val userClientMock: UserClient = mock()

  lateinit var viewModel: PinAuthenticationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = PinAuthenticationViewModel(
      isSdkInitializedUseCase,
      getRegisteredAuthenticatorsUseCase,
      pinAuthenticationUseCase,
      getUserProfilesUseCase,
      pinAuthenticationRequestHandler,
      getAuthenticatedUserProfileUseCase
    )
  }

  @Test
  fun `Given sdk is not initialized, When LoadData event is sent, Then default state should be returned`() {
    val expectedState = viewModel.uiState.copy(null, false, emptySet(), null, false, null)

    viewModel.onEvent(LoadData)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When LoadData event is sent, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true)
    mockSdkInitialized()

    viewModel.onEvent(LoadData)

    assertThat(viewModel.uiState).isEqualTo(expectedState)

  }

  @Test
  fun `Given sdk is initialized and user profiles are present, When LoadData event is sent, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true
    )
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    viewModel.onEvent(LoadData)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, registered and authenticated user profiles are present, When LoadData event is sent, Then data should be updated`() {
    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfiles = TEST_USER_PROFILES,
      selectedUserProfile = TEST_USER_PROFILES.first(),
      isAuthenticateButtonEnabled = true,
      authenticatedUserProfile = TEST_USER_PROFILE_1
    )
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockAuthenticatedProfile()

    viewModel.onEvent(LoadData)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected identity providers event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedUserProfile = TEST_USER_PROFILE_1)

    viewModel.onEvent(UpdateSelectedUserProfile(TEST_USER_PROFILE_1))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }


  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }

  private fun mockAuthenticatedProfile() {
    whenever(userClientMock.authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }
}
