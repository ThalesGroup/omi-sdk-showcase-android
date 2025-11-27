package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiResetHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiResetError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.SdkResetUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.sdkreset.SdkResetViewModel
import com.onewelcome.showcaseapp.feature.sdkreset.SdkResetViewModel.State
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
class SdkResetViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  private lateinit var viewModel: SdkResetViewModel

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var sdkResetUseCase: SdkResetUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val mockOneginiResetError = mock<OneginiResetError>()
  private val mockUserClient = mock<UserClient>()
  private val mockUserProfile = mock<UserProfile>()

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(mockUserClient)
  }

  @Test
  fun `When viewmodel is initialized, Then state should be initial`() {
    initializeViewModel()

    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)
  }

  @Test
  fun `Given SDK is not initialized, When viewmodel is initialized, Then state should not be updated`() {
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
  fun `When ResetSdk event is sent and finishes successfully, Then state should be updated`() {
    mockSdkInitialized()
    mockSuccessfulReset()

    initializeViewModel()
    viewModel.onEvent(SdkResetViewModel.UiEvent.ResetSdk)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Ok(Unit)
      )
    )
  }

  @Test
  fun `When ResetSdk event is sent and finishes with error, Then state should be updated`() {
    mockSdkInitialized()
    mockUnsuccessfulReset()

    initializeViewModel()
    viewModel.onEvent(SdkResetViewModel.UiEvent.ResetSdk)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Err(mockOneginiResetError)
      )
    )
  }

  @Test
  fun `When ResetSdk event is sent, Then loading state should be updated`() {
    mockSdkInitialized()
    whenever(oneginiClientMock.reset(any()))
      .thenAnswer { invocation ->
        assertThat(viewModel.uiState.isLoading).isTrue()
        invocation.getArgument<OneginiResetHandler>(0).onSuccess(emptySet())
      }

    initializeViewModel()
    viewModel.onEvent(SdkResetViewModel.UiEvent.ResetSdk)

    assertThat(viewModel.uiState.isLoading).isFalse()
  }

  @Test
  fun `Given SDK is not initialized, When ResetSdk event is sent, Then state should be updated`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")

    initializeViewModel()
    viewModel.onEvent(SdkResetViewModel.UiEvent.ResetSdk)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(INITIAL_STATE.copy(result = Err(expectedException)))
  }

  @Test
  fun `Given user is authenticated, When viewmodel is initialized, Then state should contain authenticated user profile`() {
    mockSdkInitialized()
    whenever(mockUserClient.authenticatedUserProfile).thenReturn(mockUserProfile)

    initializeViewModel()

    assertThat(viewModel.uiState.authenticatedUserProfile).isEqualTo(mockUserProfile)
  }

  @Test
  fun `Given user is authenticated, When ResetSdk event is sent and finishes successfully, Then state should not contain authenticated user profile`() {
    mockSdkInitialized()
    whenever(mockUserClient.authenticatedUserProfile).thenReturn(mockUserProfile)
    mockSuccessfulReset()

    initializeViewModel()
    
    whenever(mockUserClient.authenticatedUserProfile).thenReturn(null)
    
    viewModel.onEvent(SdkResetViewModel.UiEvent.ResetSdk)

    assertThat(viewModel.uiState.authenticatedUserProfile).isNull()
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  private fun initializeViewModel() {
    viewModel = SdkResetViewModel(sdkResetUseCase, isSdkInitializedUseCase, getAuthenticatedUserProfileUseCase)
  }

  private fun mockSuccessfulReset() {
    whenever(oneginiClientMock.reset(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiResetHandler>(0).onSuccess(emptySet())
      }
  }

  private fun mockUnsuccessfulReset() {
    whenever(oneginiClientMock.reset(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiResetHandler>(0).onError(mockOneginiResetError)
      }
  }

  companion object {
    private val INITIAL_STATE = State(
      result = null,
      isSdkInitialized = false,
      isLoading = false,
      authenticatedUserProfile = null
    )
  }
}
