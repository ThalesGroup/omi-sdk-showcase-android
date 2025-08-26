package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiLogoutHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiLogoutError
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.LogoutUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.logout.LogoutViewModel
import com.onewelcome.showcaseapp.feature.logout.LogoutViewModel.State
import com.onewelcome.showcaseapp.utils.ThrowableEquals
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class LogoutViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  private lateinit var viewModel: LogoutViewModel

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var logoutUseCase: LogoutUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()

  private val mockOneginiLogoutError = mock<OneginiLogoutError>()

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  @Test
  fun `When viewmodel is initialized, Then state should be initial`() {
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)
  }

  @Test
  fun `Given SDK is initialized, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()

    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE.copy(isSdkInitialized = true))
  }

  @Test
  fun `Given SDK is initialized and authenticated user profile is present, When viewmodel is initialized, Then state should be updated`() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile()

    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        authenticatedUserProfile = TEST_USER_PROFILE_1
      )
    )
  }

  @Test
  fun `When Logout event is sent and finishes successfully, Then state should be updated`() {
    mockSdkInitialized()
    mockSuccessfulLogout()

    initializeViewModel()
    viewModel.onEvent(LogoutViewModel.UiEvent.LogoutUser)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Ok(Unit)
      )
    )
  }

  @Test
  fun `When Logout event is sent and finishes with error, Then state should be updated`() {
    mockSdkInitialized()
    mockUnsuccessfulLogout()

    initializeViewModel()
    viewModel.onEvent(LogoutViewModel.UiEvent.LogoutUser)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Err(mockOneginiLogoutError)
      )
    )
  }

  @Test
  fun `When logout event is sent, Then loading state should be updated`() {
    mockSdkInitialized()
    whenever(userClientMock.logout(any()))
      .thenAnswer { invocation ->
        assertThat(viewModel.uiState.isLoading).isTrue()
        invocation.getArgument<OneginiLogoutHandler>(0).onSuccess()
      }

    initializeViewModel()
    viewModel.onEvent(LogoutViewModel.UiEvent.LogoutUser)

    assertThat(viewModel.uiState.isLoading).isFalse()
  }

  @Test
  fun `Given SDK is not initialized, When Logout event is sent, Then state should be updated`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")

    initializeViewModel()
    viewModel.onEvent(LogoutViewModel.UiEvent.LogoutUser)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(INITIAL_STATE.copy(result = Err(expectedException)))
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun mockAuthenticatedUserProfile() {
    whenever(omiSdkEngineFake.oneginiClient.getUserClient().authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun initializeViewModel() {
    viewModel = LogoutViewModel(isSdkInitializedUseCase, getAuthenticatedUserProfileUseCase, logoutUseCase)
  }

  private fun mockSuccessfulLogout() {
    whenever(userClientMock.logout(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiLogoutHandler>(0).onSuccess()
      }
  }

  private fun mockUnsuccessfulLogout() {
    whenever(userClientMock.logout(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiLogoutHandler>(0).onError(mockOneginiLogoutError)
      }
  }

  companion object {
    private val INITIAL_STATE = State(
      result = null,
      isSdkInitialized = false,
      authenticatedUserProfile = null,
      isLoading = false,
    )
  }
}
