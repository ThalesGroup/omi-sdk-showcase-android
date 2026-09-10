package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.OneginiStatelessRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.actions.QrCodeRegistrationAction
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.usecase.GetCustomIdentityProvidersUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.StatelessUserRegistrationUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_CUSTOM_INFO
import com.onewelcome.core.util.TestConstants.TEST_API_IDENTITY_PROVIDERS as TEST_IDENTITY_PROVIDERS
import com.onewelcome.core.util.TestConstants.TEST_SELECTED_API_IDENTITY_PROVIDER as TEST_SELECTED_IDENTITY_PROVIDER
import com.onewelcome.core.util.TestConstants.TEST_SELECTED_SCOPES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES_IDS
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.FakePinCallback
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.CancelRegistration
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.SetStatelessRegistration
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.StartOneStepRegistration
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.UpdateSelectedIdentityProvider
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.UpdateSelectedScopes
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent.UseDefaultIdentityProvider
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class OneStepRegistrationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var qrCodeRegistrationAction: QrCodeRegistrationAction

  @Inject
  lateinit var createPinRequestHandler: CreatePinRequestHandler

  @Inject
  lateinit var userRegistrationUseCase: UserRegistrationUseCase

  @Inject
  lateinit var statelessUserRegistrationUseCase: StatelessUserRegistrationUseCase

  @Inject
  lateinit var getCustomIdentityProvidersUseCase: GetCustomIdentityProvidersUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  private val mockOneginiRegistrationError: OneginiRegistrationError = mock()
  private val pinCallback = FakePinCallback()
  private val userClientMock: UserClient = mock()

  private lateinit var viewModel: OneStepRegistrationViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
    viewModel = createViewModel()
  }

  private fun createViewModel() = OneStepRegistrationViewModel(
    isSdkInitializedUseCase,
    userRegistrationUseCase,
    statelessUserRegistrationUseCase,
    getCustomIdentityProvidersUseCase,
    getUserProfilesUseCase,
    createPinRequestHandler,
    qrCodeRegistrationAction
  )

  // ─── Initialization ───────────────────────────────────────────────────────

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then default state should be returned`() {
    val expectedState = viewModel.uiState.copy(
      result = null,
      identityProviders = emptySet(),
      isSdkInitialized = false,
      selectedIdentityProvider = null,
      selectedScopes = Constants.DEFAULT_SCOPES,
      shouldUseDefaultIdentityProvider = false,
      userProfileIds = emptyList(),
      isRegistrationCancellationEnabled = false
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are user profiles and identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockCustomIdentityProviders()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      identityProviders = TEST_IDENTITY_PROVIDERS,
      userProfileIds = TEST_USER_PROFILES_IDS,
      selectedIdentityProvider = TEST_SELECTED_IDENTITY_PROVIDER
    )

    viewModel = createViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are user profiles, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = TEST_USER_PROFILES_IDS
    )

    viewModel = createViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockCustomIdentityProviders()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      identityProviders = TEST_IDENTITY_PROVIDERS,
      selectedIdentityProvider = TEST_SELECTED_IDENTITY_PROVIDER
    )

    viewModel = createViewModel()

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  // ─── UI Events ────────────────────────────────────────────────────────────

  @Test
  fun `When update selected identity provider event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedIdentityProvider = TEST_SELECTED_IDENTITY_PROVIDER)

    viewModel.onEvent(UpdateSelectedIdentityProvider(TEST_SELECTED_IDENTITY_PROVIDER))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected scopes event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedScopes = TEST_SELECTED_SCOPES)

    viewModel.onEvent(UpdateSelectedScopes(TEST_SELECTED_SCOPES))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When use default identity provider event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(shouldUseDefaultIdentityProvider = true)

    viewModel.onEvent(UseDefaultIdentityProvider(true))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When set stateless registration event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(isStatelessRegistration = true)

    viewModel.onEvent(SetStatelessRegistration(true))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  // ─── Registration (Normal) ────────────────────────────────────────────────

  @Test
  fun `Given sdk is not initialized, When start registration event is sent, Then error should be returned`() {
    val expectedState = viewModel.uiState.copy(result = Err(IllegalStateException("Onegini SDK instance not yet initialized")))

    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When start registration event is sent and sdk returns success, Then state should be updated with success result`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserSuccessfully()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      result = Ok(Pair(TEST_USER_PROFILE_1, TEST_CUSTOM_INFO)),
      userProfileIds = TEST_USER_PROFILES_IDS
    )

    viewModel = createViewModel()
    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When start registration event is sent and sdk returns error, Then state should be updated with error result`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserUnsuccessfully()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      result = Err(mockOneginiRegistrationError)
    )

    viewModel = createViewModel()
    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and should use default identity provider, When start registration event is sent, Then should pass identity provider as null`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(UseDefaultIdentityProvider(true))
    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(null)
    }
  }

  @Test
  fun `Given sdk is initialized and should use selected identity provider, When start registration event is sent, Then should pass selected identity provider`() {
    mockSdkInitialized()
    mockUserClient()
    mockCustomIdentityProviders()

    viewModel.onEvent(UpdateSelectedIdentityProvider(TEST_SELECTED_IDENTITY_PROVIDER))
    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_IDENTITY_PROVIDER)
    }
  }

  @Test
  fun `Given sdk is initialized and default scopes are selected, When start registration event is sent, Then should pass default scopes`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<Array<String?>> {
      verify(userClientMock).registerUser(anyOrNull(), capture(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_SCOPES.toTypedArray())
    }
  }

  @Test
  fun `Given sdk is initialized, When start registration event is sent, Then isRegistrationCancellationEnabled value should behave properly`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserSuccessfully()
    mockUserProfiles()
    whenever(userClientMock.registerUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(true)
        invocation.getArgument<OneginiRegistrationHandler>(2).onSuccess(TEST_USER_PROFILE_1, TEST_CUSTOM_INFO)
      }

    assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(false)

    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(false)
  }

  @Test
  fun `When cancel registration event is sent, Then pin creation should be cancelled`() {
    createPinRequestHandler.startPinCreation(TEST_USER_PROFILE_1, pinCallback, 5)

    viewModel.onEvent(CancelRegistration)
  }

  // ─── Navigation Events ────────────────────────────────────────────────────

  @Test
  fun `Given pin creation is in progress, When start registration event is sent, Then navigation to pin screen should be emitted`() {
    val expected = NavigationEvent.ToPinScreen
    createPinRequestHandler.startPinCreation(TEST_USER_PROFILE_1, pinCallback, 5)

    viewModel.onEvent(StartOneStepRegistration)

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(expected)
    }
  }

  // ─── Stateless Registration ───────────────────────────────────────────────

  @Test
  fun `Given sdk is not initialized and in stateless mode, When start registration event is sent, Then error should be returned`() {
    val expectedState = viewModel.uiState.copy(
      isStatelessRegistration = true,
      result = Err(IllegalStateException("Onegini SDK instance not yet initialized"))
    )

    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and in stateless mode, When start registration event is sent and sdk returns success, Then state should be updated with success result`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredStatelessUserSuccessfully()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      isStatelessRegistration = true,
      result = Ok(Pair(UserProfile.stateless, TEST_CUSTOM_INFO))
    )

    viewModel = createViewModel()
    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and in stateless mode, When start registration event is sent and sdk returns error, Then state should be updated with error result`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredStatelessUserUnsuccessfully()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      isStatelessRegistration = true,
      result = Err(mockOneginiRegistrationError)
    )

    viewModel = createViewModel()
    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(StartOneStepRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and should use default identity provider and in stateless mode, When start registration event is sent, Then should pass identity provider as null`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(UseDefaultIdentityProvider(true))
    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerStatelessUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(null)
    }
  }

  @Test
  fun `Given sdk is initialized and should use selected identity provider and in stateless mode, When start registration event is sent, Then should pass selected identity provider`() {
    mockSdkInitialized()
    mockUserClient()
    mockCustomIdentityProviders()

    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(UpdateSelectedIdentityProvider(TEST_SELECTED_IDENTITY_PROVIDER))
    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerStatelessUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_IDENTITY_PROVIDER)
    }
  }

  @Test
  fun `Given sdk is initialized and default scopes are selected and in stateless mode, When start registration event is sent, Then should pass default scopes`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(SetStatelessRegistration(true))
    viewModel.onEvent(StartOneStepRegistration)

    argumentCaptor<Array<String?>> {
      verify(userClientMock).registerStatelessUser(anyOrNull(), capture(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_SCOPES.toTypedArray())
    }
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private fun whenRegisteredUserSuccessfully() {
    whenever(userClientMock.registerUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiRegistrationHandler>(2).onSuccess(TEST_USER_PROFILE_1, TEST_CUSTOM_INFO)
      }
  }

  private fun whenRegisteredUserUnsuccessfully() {
    whenever(userClientMock.registerUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiRegistrationHandler>(2).onError(mockOneginiRegistrationError)
      }
  }

  private fun whenRegisteredStatelessUserSuccessfully() {
    whenever(userClientMock.registerStatelessUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiStatelessRegistrationHandler>(2).onSuccess(TEST_CUSTOM_INFO)
      }
  }

  private fun whenRegisteredStatelessUserUnsuccessfully() {
    whenever(userClientMock.registerStatelessUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiStatelessRegistrationHandler>(2).onError(mockOneginiRegistrationError)
      }
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockCustomIdentityProviders() {
    whenever(userClientMock.identityProviders).thenReturn(TEST_IDENTITY_PROVIDERS)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }
}
