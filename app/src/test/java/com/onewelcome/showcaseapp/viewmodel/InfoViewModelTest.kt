package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.Constants.TEST_USER_PROFILES
import com.onewelcome.core.util.Constants.TEST_USER_PROFILES_IDS
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.info.InfoViewModel
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
class InfoViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getUserProfilesUseCase: GetUserProfilesUseCase

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var userClientMock: UserClient

  private lateinit var viewModel: InfoViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = InfoViewModel(isSdkInitializedUseCase, getUserProfilesUseCase)
  }

  @Test
  fun `Given sdk is not initialized, When viewmodel is initialized, Then state should be updated`() {
    val expectedState = viewModel.uiState.copy(isSdkInitialized = false, userProfileIds = Err(Unit))

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are no user profiles, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true, userProfileIds = Err(Unit))

    viewModel = InfoViewModel(isSdkInitializedUseCase, getUserProfilesUseCase)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `Given sdk is initialized and there are user profiles, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()
    mockUserProfileIds()
    val expectedState = viewModel.uiState.copy(isSdkInitialized = true, userProfileIds = Ok(TEST_USER_PROFILES_IDS))

    viewModel = InfoViewModel(isSdkInitializedUseCase, getUserProfilesUseCase)

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(OmiSdkInitializationSettings(true, null, null, null))
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockUserClient() {
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  private fun mockUserProfileIds() {
    whenever(userClientMock.userProfiles).thenReturn(TEST_USER_PROFILES)
  }
}
