package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.GetBrowserIdentityProvidersUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.Constants
import com.onewelcome.core.util.Constants.TEST_CUSTOM_INFO
import com.onewelcome.core.util.Constants.TEST_IDENTITY_PROVIDERS
import com.onewelcome.core.util.Constants.TEST_SELECTED_IDENTITY_PROVIDER
import com.onewelcome.core.util.Constants.TEST_SELECTED_SCOPES
import com.onewelcome.core.util.Constants.TEST_USER_PROFILES
import com.onewelcome.core.util.Constants.TEST_USER_PROFILES_IDS
import com.onewelcome.core.util.Constants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent.StartBrowserRegistration
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent.UpdateSelectedIdentityProvider
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent.UpdateSelectedScopes
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent.UseDefaultIdentityProvider
import com.onewelcome.showcaseapp.utils.ResultAssert
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
class BrowserRegistrationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var browserRegistrationUseCase: BrowserRegistrationUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

  @Inject
  lateinit var getBrowserIdentityProvidersUseCase: GetBrowserIdentityProvidersUseCase

  @Inject
  lateinit var oneginiConfigModel: OneginiConfigModel

  private val mockOneginiRegistrationError: OneginiRegistrationError = mock()

  private val userClientMock: UserClient = mock()

  private lateinit var viewModel: BrowserRegistrationViewModel


  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )
  }

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then default state should be returned`() {
    val expectedState = viewModel.uiState.copy(null, emptySet(), false, null, Constants.DEFAULT_SCOPES, false, emptyList(), false)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new user profiles and identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      identityProviders = TEST_IDENTITY_PROVIDERS,
      userProfileIds = TEST_USER_PROFILES_IDS,
      selectedIdentityProvider = TEST_SELECTED_IDENTITY_PROVIDER
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new user profiles, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      userProfileIds = TEST_USER_PROFILES_IDS
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      identityProviders = TEST_IDENTITY_PROVIDERS,
      selectedIdentityProvider = TEST_SELECTED_IDENTITY_PROVIDER
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and registration is in progress, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockRegistrationIsInProgress()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      isRegistrationCancellationEnabled = true
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected identity providers event is sent, Then updated state should be returned`() {
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
  fun `Given sdk is not initialized, When register event is sent, Then error should be returned`() {
    viewModel.onEvent(StartBrowserRegistration)

    ResultAssert
      .assertThat(viewModel.uiState.result!!)
      .hasErrorInstance(IllegalStateException::class.java, "Onegini SDK instance not yet initialized")
  }

  @Test
  fun `Given sdk is initialized, When register event is sent, Then should successfully register user and update user profiles`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserSuccessfully()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      result = Ok(Pair(TEST_USER_PROFILE_1, TEST_CUSTOM_INFO)),
      userProfileIds = TEST_USER_PROFILES_IDS
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )
    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When register event is sent and sdk returns error, Then state should be updated accordingly`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserUnsuccessfully()

    val expectedState = viewModel.uiState.copy(
      isSdkInitialized = true,
      result = Err(mockOneginiRegistrationError)
    )

    viewModel = BrowserRegistrationViewModel(
      isSdkInitializedUseCase,
      browserRegistrationUseCase,
      getBrowserIdentityProvidersUseCase,
      getUserProfilesUseCase,
      oneginiConfigModel,
      browserRegistrationRequestHandler
    )
    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and should use default identity provider, When register event is sent, Then should pass identity provider as null`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(UseDefaultIdentityProvider(true))
    viewModel.onEvent(StartBrowserRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(null)
    }
  }

  @Test
  fun `Given sdk is initialized and should use selected identity provider, When register event is sent, Then should pass identity provider as selected identity provider`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()

    viewModel.onEvent(UpdateSelectedIdentityProvider(TEST_SELECTED_IDENTITY_PROVIDER))
    viewModel.onEvent(StartBrowserRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_IDENTITY_PROVIDER)
    }
  }


  @Test
  fun `Given sdk is initialized and default scopes are selected, When register event is sent, Then should pass default scopes`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(StartBrowserRegistration)

    argumentCaptor<Array<String?>> {
      verify(userClientMock).registerUser(anyOrNull(), capture(), any())
      assertThat(firstValue).isEqualTo(TEST_SELECTED_SCOPES.toTypedArray())
    }
  }

  @Test
  fun `When cancel registration event is sent, Then sdk handler should call cancel registration`() {
    viewModel.onEvent(BrowserRegistrationViewModel.UiEvent.CancelRegistration)

    verify(browserRegistrationRequestHandler).cancelRegistration()
  }

  @Test
  fun `Given sdk is initialized, When register event is sent, Then isRegistrationCancellationEnabled value should behave properly`() {
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

    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(false)
  }


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

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockBrowserIdentityProviders() {
    whenever(userClientMock.identityProviders).thenReturn(TEST_IDENTITY_PROVIDERS)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }

  private fun mockRegistrationIsInProgress() {
    whenever(browserRegistrationUseCase.isRegistrationInProgress()).thenReturn(true)
  }
}
