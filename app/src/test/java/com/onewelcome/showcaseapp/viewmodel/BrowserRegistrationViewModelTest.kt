package com.onewelcome.showcaseapp.viewmodel

import android.os.Parcel
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.BrowserIdentityProvider
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
  }

  @Test
  fun `should update UI state on init`() {
    mockSdkInitialized()
    mockUserClient()
    mockBrowserIdentityProviders()
    mockUserProfiles()

    viewModel = BrowserRegistrationViewModel(isSdkInitializedUseCase, browserRegistrationUseCase, getUserProfilesUseCase)

    val expectedState = viewModel.uiState.copy(
      identityProviders = identityProviderResult,
      userProfiles = userProfilesResult
    )

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(omiSdkEngineFake.oneginiClient.getUserClient()).thenReturn(userClientMock)

  }

  private fun mockBrowserIdentityProviders() {
    whenever(userClientMock.identityProviders).thenReturn(identityProviders)
  }

  private fun mockUserProfiles() {
    whenever(userClientMock.userProfiles).thenReturn(userProfiles)
  }

  companion object {
    private val BrowserIdentityProvider1 = object : OneginiIdentityProvider {
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

    private val BrowserIdentityProvider2 = object : OneginiIdentityProvider {
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

    private val ApiIdentityProvider1 = object : OneginiIdentityProvider {
      override val id: String
        get() = "API-identity-provider-id-1"
      override val name: String
        get() = "API identity provider name 1"

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

    private val identityProviders = setOf(BrowserIdentityProvider1, BrowserIdentityProvider2)
    private val userProfiles = setOf(USER_PROFILE_1, USER_PROFILE_2)
    private val identityProviderResult = identityProviders.map { BrowserIdentityProvider(it.name, it.id) }.toList()
    private val userProfilesResult = userProfiles.map { it.profileId }.toList()
  }
}
