package com.onewelcome.showcaseapp.viewmodel

import androidx.biometric.BiometricPrompt
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticationError
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBiometricCallback
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.BiometricAuthenticationHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.BiometricAuthenticationUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_2
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.utils.withEqualsForThrowable
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class BiometricAuthenticationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase

  @Inject
  lateinit var biometricAuthenticationUseCase: BiometricAuthenticationUseCase

  @Inject
  lateinit var biometricAuthenticationHandler: BiometricAuthenticationHandler

  @Inject
  lateinit var pinAuthenticationRequestHandler: PinAuthenticationRequestHandler

  private val userClientMock: UserClient = mock()

  private lateinit var viewModel: BiometricAuthenticationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then default state should be returned`() {
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE)
  }

  @Test
  fun `Given sdk is initialized, When viewmodel is initialized, Then data should be updated`() {
    mockSdkInitialized()
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE.copy(isSdkInitialized = true))
  }

  @Test
  fun `Given sdk is initialized and user profiles are present, When viewmodel is initialized, Then data should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isSdkInitialized = true,
        userProfiles = TEST_USER_PROFILES,
        selectedUserProfile = TEST_USER_PROFILES.first(),
        isAuthenticateButtonEnabled = true
      )
    )
  }

  @Test
  fun `Given sdk is initialized, registered and authenticated user profiles are present, When viewmodel is initialized, Then data should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockAuthenticatedProfile()
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isSdkInitialized = true,
        userProfiles = TEST_USER_PROFILES,
        selectedUserProfile = TEST_USER_PROFILES.first(),
        isAuthenticateButtonEnabled = true,
        authenticatedUserProfile = TEST_USER_PROFILE_1
      )
    )
  }

  @Test
  fun `When update selected user profile event is sent, Then data should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    initializeViewModel()

    assertThat(viewModel.uiState.selectedUserProfile).isEqualTo(TEST_USER_PROFILE_1)
    viewModel.onEvent(UiEvent.UpdateSelectedUserProfile(TEST_USER_PROFILE_2))

    assertThat(viewModel.uiState.selectedUserProfile).isEqualTo(TEST_USER_PROFILE_2)
  }

  @Test
  fun `Given no user profile is selected, When start biometric authentication event is sent, Then error should be returned`() {
    val expectedError = Err(IllegalArgumentException("User profile not selected"))
    mockSdkInitialized()
    mockUserClient()
    initializeViewModel()

    viewModel.onEvent(UiEvent.StartBiometricAuthentication)

    assertThat(viewModel.uiState.result)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(expectedError)
  }

  @Test
  fun `Given user profile is selected, When start biometric authentication event is sent and finished successfully, Then data should be updated`() {
    val expectedCustomInfo = CustomInfo(200, "test")
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockAuthenticationSuccess(expectedCustomInfo)
    initializeViewModel()

    viewModel.onEvent(UiEvent.StartBiometricAuthentication)

    assertThat(viewModel.uiState).isEqualTo(
      DEFAULT_STATE.copy(
        isSdkInitialized = true,
        userProfiles = TEST_USER_PROFILES,
        selectedUserProfile = TEST_USER_PROFILE_1,
        isAuthenticateButtonEnabled = true,
        authenticatedUserProfile = TEST_USER_PROFILE_1,
        result = Ok(TEST_USER_PROFILE_1 to expectedCustomInfo)
      )
    )
  }

  @Test
  fun `Given user profile is selected, When start biometric authentication event is sent and not biometric authenticator registered, Then error should be returned`() {
    val expectedError = Err(Exception("Biometric authenticator not found for user"))
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockNoAuthenticatorsRegistered()
    initializeViewModel()

    viewModel.onEvent(UiEvent.StartBiometricAuthentication)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          userProfiles = TEST_USER_PROFILES,
          selectedUserProfile = TEST_USER_PROFILE_1,
          isAuthenticateButtonEnabled = true,
          result = expectedError
        )
      )
  }

  @Test
  fun `Given user profile is selected, When start biometric authentication event is sent and finishes with error, Then error should be returned`() {
    val expectedError: OneginiAuthenticationError = mock()
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    mockAuthenticationError(expectedError)
    initializeViewModel()

    viewModel.onEvent(UiEvent.StartBiometricAuthentication)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(
        DEFAULT_STATE.copy(
          isSdkInitialized = true,
          userProfiles = TEST_USER_PROFILES,
          selectedUserProfile = TEST_USER_PROFILE_1,
          isAuthenticateButtonEnabled = true,
          result = Err(expectedError)
        )
      )
  }

  @Test
  fun `When biometric authentication is started, Then should show biometric prompt`() = runTest {
    val cryptoObjectMock: BiometricPrompt.CryptoObject = mock()
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    initializeViewModel()

    viewModel.onEvent(UiEvent.StartBiometricAuthentication)
    biometricAuthenticationHandler.startAuthentication(TEST_USER_PROFILE_1, cryptoObjectMock, mock())

    assertThat(viewModel.navigationEvents.first())
      .isEqualTo(NavigationEvent.ShowBiometricPrompt(cryptoObjectMock))
  }

  @Test
  fun `Given user authenticated, When BiometricAuthenticationSuccess event is sent, Then should call biometric handler success callback`() {
    val callbackMock: OneginiBiometricCallback = mock()
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    initializeViewModel()

    biometricAuthenticationHandler.startAuthentication(TEST_USER_PROFILE_1, mock(), callbackMock)
    viewModel.onEvent(UiEvent.BiometricAuthenticationSuccess)

    verify(callbackMock).userAuthenticatedSuccessfully()
  }

  @Test
  fun `Given user authenticated, When BiometricAuthenticationError event is sent, Then should call biometric handler error callback`() {
    val callbackMock: OneginiBiometricCallback = mock()
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    initializeViewModel()

    biometricAuthenticationHandler.startAuthentication(TEST_USER_PROFILE_1, mock(), callbackMock)
    viewModel.onEvent(UiEvent.BiometricAuthenticationError(10000))

    verify(callbackMock).onBiometricAuthenticationError(10000)
  }

  @Test
  fun `When fallback to pin is sent, Then pin authentication should be triggered`() = runTest {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()
    initializeViewModel()

    pinAuthenticationRequestHandler.startAuthentication(TEST_USER_PROFILE_1, mock(), mock())

    assertThat(viewModel.navigationEvents.first())
      .isEqualTo(NavigationEvent.ToPinScreen)
  }


  private fun initializeViewModel() {
    viewModel = BiometricAuthenticationViewModel(
      isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase,
      getUserProfilesUseCase,
      getRegisteredAuthenticatorsUseCase,
      biometricAuthenticationUseCase,
      biometricAuthenticationHandler,
      pinAuthenticationRequestHandler
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

  private fun mockAuthenticatedProfile() {
    whenever(userClientMock.authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun mockAuthenticationSuccess(expectedCustomInfo: CustomInfo) {
    val biometricAuthenticator = getBiometricAuthenticator(true)
    whenever(userClientMock.getAllAuthenticators(any())).thenReturn(setOf(biometricAuthenticator))
    whenever(userClientMock.authenticateUser(eq(TEST_USER_PROFILE_1), eq(biometricAuthenticator), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticationHandler>(2).onSuccess(TEST_USER_PROFILE_1, expectedCustomInfo)
      }
  }

  private fun mockNoAuthenticatorsRegistered() {
    whenever(userClientMock.getAllAuthenticators(any())).thenReturn(emptySet())
  }

  private fun mockAuthenticationError(expectedError: OneginiAuthenticationError) {
    val biometricAuthenticator = getBiometricAuthenticator(true)
    whenever(userClientMock.getAllAuthenticators(any())).thenReturn(setOf(biometricAuthenticator))
    whenever(userClientMock.authenticateUser(eq(TEST_USER_PROFILE_1), eq(biometricAuthenticator), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticationHandler>(2).onError(expectedError)
      }
  }

  private fun getBiometricAuthenticator(isRegistered: Boolean) = object : OneginiAuthenticator {
    override val id: String = "biometric"
    override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.BIOMETRIC
    override val name: String = "BIOMETRIC"
    override val isRegistered: Boolean = isRegistered
    override val isPreferred: Boolean = false
    override val userProfile: UserProfile = TEST_USER_PROFILE_1
  }

  companion object {
    private val DEFAULT_STATE = State(
      isLoading = false,
      isSdkInitialized = false,
      userProfiles = emptySet(),
      selectedUserProfile = null,
      authenticatedUserProfile = null,
      isBiometricAuthenticatorRegisteredForUser = false,
      isAuthenticateButtonEnabled = false,
      result = null
    )
  }
}
