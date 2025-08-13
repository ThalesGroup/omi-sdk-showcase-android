package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiChangePinHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiChangePinError
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.ChangePinUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.changepin.ChangePinViewModel
import com.onewelcome.showcaseapp.feature.changepin.ChangePinViewModel.State
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
class ChangePinViewModelTest {

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
  lateinit var changePinUseCase: ChangePinUseCase

  @Inject
  lateinit var authenticationRequestHandler: PinAuthenticationRequestHandler

  @Inject
  lateinit var createPinRequestHandler: CreatePinRequestHandler

  private val userClientMock = mock<UserClient>()

  private val mockOneginiChangePinError = mock<OneginiChangePinError>()

  private lateinit var viewModel: ChangePinViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
  }

  @Test
  fun `Given SDK is not initialized, When viewmodel is initialized, Then state should be updated`() {
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
  fun `Given SDK is initialized and authenticated user profile user present, When viewmodel is initialized, Then state should be updated`() {
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
  fun `When StartPinChange event is sent and finishes successfully, Then state should be updated`() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile()
    mockSuccessfulPinChange()

    initializeViewModel()
    viewModel.onEvent(ChangePinViewModel.UiEvent.StartPinChange)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        result = Ok(Unit),
        isSdkInitialized = true,
        authenticatedUserProfile = TEST_USER_PROFILE_1
      )
    )
  }

  @Test
  fun `When StartPinChange event is sent and finishes with error, Then state should be updated`() {
    mockSdkInitialized()
    mockAuthenticatedUserProfile()
    mockUnsuccessfulPinChange()

    initializeViewModel()
    viewModel.onEvent(ChangePinViewModel.UiEvent.StartPinChange)

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        result = Err(mockOneginiChangePinError),
        isSdkInitialized = true,
        authenticatedUserProfile = TEST_USER_PROFILE_1
      )
    )
  }

  @Test
  fun `Given SDK is not initialized, When StartPinChange event is sent, Then state should be updated`() {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")

    initializeViewModel()
    viewModel.onEvent(ChangePinViewModel.UiEvent.StartPinChange)

    assertThat(viewModel.uiState)
      .usingRecursiveComparison()
      .withEqualsForType(ThrowableEquals(), Throwable::class.java)
      .isEqualTo(INITIAL_STATE.copy(result = Err(expectedException)))
  }

  private fun mockSuccessfulPinChange() {
    whenever(userClientMock.changePin(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiChangePinHandler>(0).onSuccess()
      }
  }

  private fun mockUnsuccessfulPinChange() {
    whenever(userClientMock.changePin(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiChangePinHandler>(0).onError(mockOneginiChangePinError)
      }
  }

  private fun initializeViewModel() {
    viewModel = ChangePinViewModel(
      isSdkInitializedUseCase,
      getAuthenticatedUserProfileUseCase,
      changePinUseCase,
      authenticationRequestHandler,
      createPinRequestHandler
    )
  }

  private fun mockAuthenticatedUserProfile() {
    whenever(omiSdkEngineFake.oneginiClient.getUserClient().authenticatedUserProfile).thenReturn(TEST_USER_PROFILE_1)
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.getTestDefaultSdkInitializationSettings())
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }

  companion object {
    private val INITIAL_STATE = State(
      result = null,
      isSdkInitialized = false,
      authenticatedUserProfile = null,
    )
  }
}
