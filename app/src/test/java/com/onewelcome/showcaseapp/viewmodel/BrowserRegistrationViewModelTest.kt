package com.onewelcome.showcaseapp.viewmodel

import android.os.Parcel
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.Constants
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
  lateinit var userClientMock: UserClient

  @Inject
  lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

  private val mockOneginiRegistrationError: OneginiRegistrationError = mock()

  private lateinit var viewModel: BrowserRegistrationViewModel


  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)
  }

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then default state should be returned`() {
    val expectedState = viewModel.uiState

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new user profiles and identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()
    mockUserProfiles()

    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)

    val expectedState = viewModel.uiState.copy(
      identityProviders = identityProviders,
      userProfiles = userProfilesIds
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new user profiles, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfiles()

    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)

    val expectedState = viewModel.uiState.copy(userProfiles = userProfilesIds)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are new identity providers, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()

    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)

    val expectedState = viewModel.uiState.copy(identityProviders = identityProviders)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and registration is in progress, When viewmodel is initialized, Then updated state should be returned`() {
    mockSdkInitialized()
    mockRegistrationIsInProgress()

    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)

    val expectedState = viewModel.uiState.copy(isRegistrationCancellationEnabled = true)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected identity providers event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedIdentityProvider = selectedIdentityProvider)

    viewModel.onEvent(UpdateSelectedIdentityProvider(selectedIdentityProvider))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When update selected scopes event is sent, Then updated state should be returned`() {
    val expectedState = viewModel.uiState.copy(selectedScopes = selectedScopes)

    viewModel.onEvent(UpdateSelectedScopes(selectedScopes))

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
      result = Ok(Pair(USER_PROFILE_1, CUSTOM_INFO)),
      userProfiles = userProfilesIds
    )

    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized, When register event is sent and sdk returns error, Then state should be updated accordingly`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserUnsuccessfully()
    mockUserProfiles()

    val expectedState = viewModel.uiState.copy(result = Err(mockOneginiRegistrationError))

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

    viewModel.onEvent(UpdateSelectedIdentityProvider(selectedIdentityProvider))
    viewModel.onEvent(StartBrowserRegistration)

    argumentCaptor<OneginiIdentityProvider> {
      verify(userClientMock).registerUser(capture(), anyOrNull(), any())
      assertThat(firstValue).isEqualTo(selectedIdentityProvider)
    }
  }


  @Test
  fun `Given sdk is initialized and default scopes are selected, When register event is sent, Then should pass default scopes`() {
    mockSdkInitialized()
    mockUserClient()

    viewModel.onEvent(StartBrowserRegistration)

    argumentCaptor<Array<String?>> {
      verify(userClientMock).registerUser(anyOrNull(), capture(), any())
      assertThat(firstValue).isEqualTo(selectedScopes.toTypedArray())
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
        invocation.getArgument<OneginiRegistrationHandler>(2).onSuccess(USER_PROFILE_1, CUSTOM_INFO)
      }

    assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(false)

    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState.isRegistrationCancellationEnabled).isEqualTo(false)
  }


  private fun whenRegisteredUserSuccessfully() {
    whenever(userClientMock.registerUser(anyOrNull(), anyOrNull(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiRegistrationHandler>(2).onSuccess(USER_PROFILE_1, CUSTOM_INFO)
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
    whenever(userClientMock.identityProviders).thenReturn(identityProviders)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(userProfiles)
  }

  private fun mockRegistrationIsInProgress() {
    whenever(browserRegistrationUseCase.isRegistrationInProgress()).thenReturn(true)
  }

  companion object {
    private val OneginiBrowserIdentityProvider1 = object : OneginiIdentityProvider {
      override val id: String
        get() = "Browser-identity-provider-id-1"
      override val name: String
        get() = "Browser identity provider name 1"

      override fun describeContents(): Int {
        return 0
      }

      override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
      }
    }

    private val OneginiBrowserIdentityProvider2 = object : OneginiIdentityProvider {
      override val id: String
        get() = "Browser-identity-provider-id-2"
      override val name: String
        get() = "Browser identity provider name 2"

      override fun describeContents(): Int {
        return 0
      }

      override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
      }
    }

    private val USER_PROFILE_1 = UserProfile("123456")
    private val USER_PROFILE_2 = UserProfile("654321")
    private val CUSTOM_INFO = CustomInfo(666, "data")
    private val userProfiles = setOf(USER_PROFILE_1, USER_PROFILE_2)
    private val userProfilesIds = userProfiles.map { it.profileId }.toList()

    private val identityProviders = setOf(OneginiBrowserIdentityProvider1, OneginiBrowserIdentityProvider2)
    private val selectedIdentityProvider = identityProviders.first()
    private val selectedScopes = Constants.DEFAULT_SCOPES
  }
}
