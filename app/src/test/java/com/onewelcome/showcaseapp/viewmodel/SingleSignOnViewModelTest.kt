package com.onewelcome.showcaseapp.viewmodel

import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiAppToWebSingleSignOnHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiAppToWebSingleSignOnError
import com.onegini.mobile.sdk.android.model.OneginiAppToWebSingleSignOn
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.SingleSignOnUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.feature.singlesignon.SingleSignOnViewModel
import com.onewelcome.showcaseapp.feature.singlesignon.SingleSignOnViewModel.State
import com.onewelcome.showcaseapp.utils.ThrowableEquals
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
class SingleSignOnViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  private lateinit var viewModel: SingleSignOnViewModel

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase

  @Inject
  lateinit var singleSignOnUseCase: SingleSignOnUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val userClientMock = mock<UserClient>()
  private val mockOneginiSsoError = mock<OneginiAppToWebSingleSignOnError>()
  private val mockSsoResult = mock<OneginiAppToWebSingleSignOn>()
  private val testUri = Uri.parse("https://example.com")
  private val testRedirectUri = Uri.parse("https://example.com/callback")

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)
    whenever(mockSsoResult.redirectUrl).thenReturn(testRedirectUri)
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
        userProfile = TEST_USER_PROFILE_1
      )
    )
  }

  @Test
  fun `When PerformSingleSignOn event is sent and finishes successfully, Then state should be updated`() {
    mockSdkInitialized()
    mockSuccessfulSso()

    initializeViewModel()
    viewModel.onEvent(SingleSignOnViewModel.Event.PerformSingleSignOn(testUri))

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Ok(mockSsoResult)
      )
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `When PerformSingleSignOn event is sent and finishes successfully, Then navigation event should be sent`() = runTest {
    mockSdkInitialized()
    mockSuccessfulSso()

    initializeViewModel()

    val events = mutableListOf<SingleSignOnViewModel.NavigationEvent>()
    val job = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.navigationEvents.collect { events.add(it) }
    }

    viewModel.onEvent(SingleSignOnViewModel.Event.PerformSingleSignOn(testUri))

    assertThat(events).hasSize(1)
    assertThat(events[0]).isInstanceOf(SingleSignOnViewModel.NavigationEvent.OpenUrl::class.java)
    assertThat((events[0] as SingleSignOnViewModel.NavigationEvent.OpenUrl).uri).isEqualTo(testRedirectUri)

    job.cancel()
  }

  @Test
  fun `When PerformSingleSignOn event is sent and finishes with error, Then state should be updated`() = runTest {
    mockSdkInitialized()
    mockUnsuccessfulSso()

    initializeViewModel()
    viewModel.onEvent(SingleSignOnViewModel.Event.PerformSingleSignOn(testUri))

    assertThat(viewModel.uiState).isEqualTo(
      INITIAL_STATE.copy(
        isSdkInitialized = true,
        result = Err(mockOneginiSsoError)
      )
    )
  }

  @Test
  fun `Given SDK is not initialized, When PerformSingleSignOn event is sent, Then state should be updated`() = runTest {
    val expectedException = IllegalStateException("Onegini SDK instance not yet initialized")

    initializeViewModel()
    viewModel.onEvent(SingleSignOnViewModel.Event.PerformSingleSignOn(testUri))

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
    viewModel = SingleSignOnViewModel(isSdkInitializedUseCase, getAuthenticatedUserProfileUseCase, singleSignOnUseCase)
  }

  private fun mockSuccessfulSso() {
    whenever(userClientMock.getAppToWebSingleSignOn(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAppToWebSingleSignOnHandler>(1).onSuccess(mockSsoResult)
      }
  }

  private fun mockUnsuccessfulSso() {
    whenever(userClientMock.getAppToWebSingleSignOn(any(), any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiAppToWebSingleSignOnHandler>(1).onError(mockOneginiSsoError)
      }
  }

  companion object {
    private val INITIAL_STATE = State(
      isSdkInitialized = false,
      userProfile = null,
      result = null
    )
  }
}
