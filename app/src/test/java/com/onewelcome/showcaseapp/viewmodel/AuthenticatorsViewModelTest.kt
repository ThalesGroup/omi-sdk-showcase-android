package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticatorDeregistrationHandler
import com.onegini.mobile.sdk.android.handlers.OneginiAuthenticatorRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticatorDeregistrationError
import com.onegini.mobile.sdk.android.handlers.error.OneginiAuthenticatorRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.DeregisterAuthenticatorUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.RegisterAuthenticatorUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorsViewModel
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorsViewModel.State
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
class AuthenticatorsViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var getAuthenticatorsUseCase: GetAuthenticatorsUseCase

  @Inject
  lateinit var registerAuthenticatorUseCase: RegisterAuthenticatorUseCase

  @Inject
  lateinit var deregisterAuthenticatorUseCase: DeregisterAuthenticatorUseCase

  @Inject
  lateinit var pinAuthenticationRequestHandler: PinAuthenticationRequestHandler

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var pinAuthenticationHandler: PinAuthenticationRequestHandler

  private val userClientMock: UserClient = mock()

  private lateinit var viewModel: AuthenticatorsViewModel

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun `Given SDK is not initialized, When view model is initialized, Then should have default state`() {
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE)
  }

  @Test
  fun `Given SDK is initialized, When view model is initialized, Then should update state`() {
    whenSdkIsInitialized()

    assertThat(viewModel.uiState).isEqualTo(DEFAULT_STATE.copy(isSdkInitialized = true))
  }

  @Test
  fun `Given user profile is authenticated, When view model is initialized, Then should update authenticated profile state`() {
    whenUserProfileIsAuthenticated()

    assertThat(viewModel.uiState.authenticatedUserProfile).isEqualTo(TEST_USER_PROFILE_1)
  }

  @Test
  fun `Given user profile is authenticated, When view model is initialized, Then should update available authenticators`() {
    val expectedAuthenticators = setOf(getPinAuthenticator(), getBiometricAuthenticator(false))
    whenUserProfileIsAuthenticated(expectedAuthenticators)

    assertThat(viewModel.uiState.availableAuthenticators).isEqualTo(expectedAuthenticators)
  }

  @Test
  fun `Given user profile is authenticated and Biometric authenticator is disabled, When ToggleAuthenticator event is sent, Then should enable Biometric authenticator`() {
    val biometricAuthenticator = getBiometricAuthenticator(false)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    verify(userClientMock).registerAuthenticator(eq(biometricAuthenticator), any())
  }

  @Test
  fun `Given user profile is authenticated and Biometric authenticator is disabled, When ToggleAuthenticator event is sent, Then should return success`() {
    val biometricAuthenticator = getBiometricAuthenticator(false)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))
    whenever(userClientMock.registerAuthenticator(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticatorRegistrationHandler>(1).onSuccess(null)
      }

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    assertThat(viewModel.uiState.result).isEqualTo(AuthenticatorsViewModel.AuthenticatorOperationResult.RegisterSuccess(null))
  }

  @Test
  fun `Given user profile is authenticated and Biometric authenticator is enabled, When ToggleAuthenticator event is sent, Then should disable Biometric authenticator`() {
    val biometricAuthenticator = getBiometricAuthenticator(true)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    verify(userClientMock).deregisterAuthenticator(eq(biometricAuthenticator), any())
  }

  @Test
  fun `Given user profile is authenticated and Biometric authenticator is enabled, When ToggleAuthenticator event is sent, Then should return success`() {
    val biometricAuthenticator = getBiometricAuthenticator(true)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))
    whenever(userClientMock.deregisterAuthenticator(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticatorDeregistrationHandler>(1).onSuccess()
      }

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    assertThat(viewModel.uiState.result).isEqualTo(AuthenticatorsViewModel.AuthenticatorOperationResult.DeregisterSuccess)
  }

  @Test
  fun `Given user profile is authenticated and Biometric authenticator is disabled, When ToggleAuthenticator event is sent, Then should navigate to pin screen`() =
    runTest {
      val biometricAuthenticator = getBiometricAuthenticator(false)
      whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))
      whenever(userClientMock.registerAuthenticator(any(), any()))
        .thenAnswer {
          pinAuthenticationHandler.startAuthentication(TEST_USER_PROFILE_1, mock(), mock())
        }

      viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

      assertThat(viewModel.navigationEvents.first()).isEqualTo(AuthenticatorsViewModel.NavigationEvent.ToPinAuthenticationScreen)
    }

  @Test
  fun `Given ToggleAuthenticator event is sent, When enabling authenticator failed, Then should update state with error result`() {
    val expectedException: OneginiAuthenticatorRegistrationError = mock()
    val biometricAuthenticator = getBiometricAuthenticator(false)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))
    whenever(userClientMock.registerAuthenticator(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticatorRegistrationHandler>(1).onError(expectedException)
      }

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    assertThat(viewModel.uiState.result).isEqualTo(AuthenticatorsViewModel.AuthenticatorOperationResult.Error(expectedException))
  }

  @Test
  fun `Given ToggleAuthenticator event is sent, When disabling authenticator failed, Then should update state with error result`() {
    val expectedException: OneginiAuthenticatorDeregistrationError = mock()
    val biometricAuthenticator = getBiometricAuthenticator(true)
    whenUserProfileIsAuthenticated(setOf(getPinAuthenticator(), biometricAuthenticator))
    whenever(userClientMock.deregisterAuthenticator(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAuthenticatorDeregistrationHandler>(1).onError(expectedException)
      }

    viewModel.onEvent(AuthenticatorsViewModel.UiEvent.ToggleAuthenticator(biometricAuthenticator))

    assertThat(viewModel.uiState.result).isEqualTo(AuthenticatorsViewModel.AuthenticatorOperationResult.Error(expectedException))
  }

  private fun whenSdkIsInitialized() {
    mockSdkInitialized()
    initializeViewModel()
  }

  private fun whenUserProfileIsAuthenticated(authenticators: Set<OneginiAuthenticator> = emptySet()) {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfileAuthenticated()
    mockAvailableAuthenticators(authenticators)
    initializeViewModel()
  }

  private fun initializeViewModel() {
    viewModel = AuthenticatorsViewModel(
      isSdkInitializedUseCase = isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase = getAuthenticatedUserProfileUseCase,
      getAuthenticatorsUseCase = getAuthenticatorsUseCase,
      registerAuthenticatorUseCase = registerAuthenticatorUseCase,
      deregisterAuthenticatorUseCase = deregisterAuthenticatorUseCase,
      pinAuthenticationRequestHandler = pinAuthenticationRequestHandler
    )
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockUserProfileAuthenticated() {
    whenever(userClientMock.authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun mockAvailableAuthenticators(authenticators: Set<OneginiAuthenticator>) {
    whenever(userClientMock.getAllAuthenticators(any())).thenReturn(authenticators)
  }

  companion object {
    private val DEFAULT_STATE = State(
      isSdkInitialized = false,
      authenticatedUserProfile = null,
      availableAuthenticators = emptySet(),
      isLoading = false,
      result = null
    )

    private fun getPinAuthenticator() = object : OneginiAuthenticator {
      override val id: String = "pin"
      override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.PIN
      override val name: String = "PIN"
      override val isRegistered: Boolean = true
      override val isPreferred: Boolean = true
      override val userProfile: UserProfile = TEST_USER_PROFILE_1
    }

    private fun getBiometricAuthenticator(isRegistered: Boolean) = object : OneginiAuthenticator {
      override val id: String = "biometric"
      override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.BIOMETRIC
      override val name: String = "BIOMETRIC"
      override val isRegistered: Boolean = isRegistered
      override val isPreferred: Boolean = false
      override val userProfile: UserProfile = TEST_USER_PROFILE_1
    }
  }
}
