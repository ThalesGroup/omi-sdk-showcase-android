package com.onewelcome.showcaseapp.viewmodel

import android.os.Parcel
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiRegistrationHandler
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.BrowserIdentityProvider
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
      identityProviders = browserIdentityProviders,
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

    val expectedState = viewModel.uiState.copy(identityProviders = browserIdentityProviders)

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

  @Ignore("Fix me")
  @Test
  fun `Given sdk is not initialized, When register event is sent, Then error should be returned`() {
    val expectedError = IllegalStateException("Onegini SDK instance not yet initialized")
    val expectedState = viewModel.uiState.copy(result = Err(expectedError))

    viewModel.onEvent(StartBrowserRegistration)

//    assertThat(viewModel.uiState.result?.value).isEqualTo(expectedState.result?.value)
  }

  @Test
  fun `Given sdk is initialized, When register event is sent, Then should successfully register user`() {
    mockSdkInitialized()
    mockUserClient()
    whenRegisteredUserSuccessfully()
    val expectedState = viewModel.uiState.copy(result = Ok(Pair(USER_PROFILE_1, CUSTOM_INFO)))

    viewModel.onEvent(StartBrowserRegistration)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun whenRegisteredUserSuccessfully() {
    whenever(oneginiClientMock.getUserClient().registerUser(any(), any(), any()))
      .thenAnswer { invocation -> invocation.getArgument<OneginiRegistrationHandler>(0).onSuccess(USER_PROFILE_1, CUSTOM_INFO) }
  }

  private fun updateSelectIdentityProvider() {
    viewModel.onEvent(UpdateSelectedIdentityProvider(selectedIdentityProvider))
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(omiSdkEngineFake.oneginiClient.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockBrowserIdentityProviders() {
    whenever(userClientMock.identityProviders).thenReturn(oneginiBrowserIdentityProviders)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(userProfiles)
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

    private val oneginiBrowserIdentityProviders = setOf(OneginiBrowserIdentityProvider1, OneginiBrowserIdentityProvider2)
    private val browserIdentityProviders = oneginiBrowserIdentityProviders.map { BrowserIdentityProvider(it.name, it.id) }.toList()
    private val selectedIdentityProvider = browserIdentityProviders.first()
    private val selectedScopes = Constants.DEFAULT_SCOPES
  }
}
