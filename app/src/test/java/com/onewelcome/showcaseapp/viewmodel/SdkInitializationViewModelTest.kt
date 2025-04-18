package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.showcaseapp.entity.OmiSdkInitializationSettings
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.usecase.OmiSdkInitializationUseCase
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel.UiEvent
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class SdkInitializationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var omiSdkInitializationUseCase: OmiSdkInitializationUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  private val oneginiInitializationError: OneginiInitializationError = mock()

  private lateinit var viewModel: SdkInitializationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = SdkInitializationViewModel(omiSdkInitializationUseCase)
  }

  @Test
  fun `should initialize sdk with default parameters`() {
    whenSdkInitializedSuccessfully()

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    argumentCaptor<OmiSdkInitializationSettings> {
      verify(omiSdkEngineFake).initialize(capture())
      assertThat(firstValue).isEqualTo(DEFAULT_SETTINGS)
    }
  }

  @Test
  fun `should successfully initialize SDK with no user profiles removed`() {
    whenSdkInitializedSuccessfully()

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    val expectedState = INITIAL_STATE.copy(result = Ok(emptySet()))
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should successfully initialize SDK with removed user profiles`() {
    whenSdkInitializedSuccessfully(REMOVED_USER_PROFILES)

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    val expectedState = INITIAL_STATE.copy(result = Ok(REMOVED_USER_PROFILES))
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should return error when initialization failed`() {
    whenSdkInitializedWithError()

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    val expectedState = INITIAL_STATE.copy(result = Err(oneginiInitializationError))
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should update shouldStoreCookies parameter successfully`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)

    val expectedValue = false
    viewModel.onEvent(UiEvent.ChangeShouldStoreCookiesValue(expectedValue))

    val expectedState = INITIAL_STATE.copy(shouldStoreCookies = expectedValue)
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should update httpConnectTimeout parameter successfully`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)

    val expectedValue = 4
    viewModel.onEvent(UiEvent.ChangeHttpConnectTimeoutValue(expectedValue))

    val expectedState = INITIAL_STATE.copy(httpConnectTimeout = expectedValue)
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should update httpReadTimeout parameter successfully`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)

    val expectedValue = 4
    viewModel.onEvent(UiEvent.ChangeHttpReadTimeoutValue(expectedValue))

    val expectedState = INITIAL_STATE.copy(httpReadTimeout = expectedValue)
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should update deviceConfigCacheDuration parameter successfully`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)

    val expectedValue = 4
    viewModel.onEvent(UiEvent.ChangeDeviceConfigCacheDurationValue(expectedValue))

    val expectedState = INITIAL_STATE.copy(deviceConfigCacheDurationSeconds = expectedValue)
    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `should show loading when initializing SDK`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)
    val expectedStateDuringInitialization = viewModel.uiState.copy(isLoading = true)
    val expectedStateAfterInitialization = expectedStateDuringInitialization.copy(isLoading = false, result = Ok(emptySet()))

    whenever(oneginiClientMock.start(any()))
      .thenAnswer { invocation ->
        assertThat(viewModel.uiState).isEqualTo(expectedStateDuringInitialization)
        invocation.getArgument<OneginiInitializationHandler>(0).onSuccess(emptySet())
      }
    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    assertThat(viewModel.uiState).isEqualTo(expectedStateAfterInitialization)
  }

  private fun whenSdkInitializedSuccessfully(removedUserProfiles: Set<UserProfile> = emptySet()) {
    whenever(oneginiClientMock.start(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiInitializationHandler>(0).onSuccess(removedUserProfiles)
      }
  }

  private fun whenSdkInitializedWithError() {
    whenever(oneginiClientMock.start(any()))
      .thenAnswer { invocation ->
        invocation.getArgument<OneginiInitializationHandler>(0).onError(oneginiInitializationError)
      }
  }

  companion object {
    private val INITIAL_STATE = SdkInitializationViewModel.State(
      shouldStoreCookies = true,
      httpConnectTimeout = null,
      httpReadTimeout = null,
      deviceConfigCacheDurationSeconds = null,
      isLoading = false,
      result = null
    )
    private val DEFAULT_SETTINGS = OmiSdkInitializationSettings(
      shouldStoreCookies = true,
      httpConnectTimeout = null,
      httpReadTimeout = null,
      deviceConfigCacheDuration = null
    )
    val REMOVED_USER_PROFILES = setOf(UserProfile("123456"), UserProfile("QWERTY"))
  }
}
