package com.onewelcome.showcaseapp.viewmodel

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.DeviceClient
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.OneginiRefreshMobileAuthPushTokenHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.usecase.NewFirebaseTokenUpdateUseCase
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.core.util.TestConstants.getTestDefaultSdkInitializationSettings
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILES
import com.onewelcome.showcaseapp.fakes.ShowcaseDataStoreFake
import com.onewelcome.showcaseapp.feature.sdkinitialization.HandlerType
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationViewModel
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationViewModel.UiEvent
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
import org.mockito.kotlin.times
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
  lateinit var newFirebaseTokenUpdateUseCase: NewFirebaseTokenUpdateUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkFacade

  @Inject
  lateinit var showcaseDataStoreFake: ShowcaseDataStoreFake

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

  private val deviceClientMock = mock<DeviceClient>()
  private val oneginiInitializationError: OneginiInitializationError = mock()

  private lateinit var viewModel: SdkInitializationViewModel

  @Before
  fun setup() {
    hiltRule.inject()
    whenever(oneginiClientMock.getDeviceClient()).thenReturn(deviceClientMock)
    viewModel = SdkInitializationViewModel(omiSdkInitializationUseCase, newFirebaseTokenUpdateUseCase, browserRegistrationRequestHandler)
  }

  @Test
  fun `should initialize sdk with default parameters`() {
    val expectedValue = getTestDefaultSdkInitializationSettings(browserRegistrationRequestHandler)
    whenSdkInitializedSuccessfully()

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    argumentCaptor<OmiSdkInitializationSettings> {
      verify(omiSdkEngineFake).initialize(capture())
      assertThat(firstValue).isEqualTo(expectedValue)
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
    whenSdkInitializedSuccessfully(TEST_USER_PROFILES)

    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    val expectedState = INITIAL_STATE.copy(result = Ok(TEST_USER_PROFILES))
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

  @Test
  fun `should not refresh mobile auth push token when there is no new firebase token`() {
    whenSdkInitializedSuccessfully()

    showcaseDataStoreFake.isFirebaseTokenUpdateNeeded = false
    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    verify(deviceClientMock, times(0)).refreshMobileAuthPushToken(any(), any())
  }

  @Test
  fun `should not refresh mobile auth push token when sdk initialization failed`() {
    whenSdkInitializedWithError()

    showcaseDataStoreFake.isFirebaseTokenUpdateNeeded = false
    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    verify(deviceClientMock, times(0)).refreshMobileAuthPushToken(any(), any())
  }

  @Test
  fun `should refresh mobile auth push token when there is new firebase token`() {
    whenMobileAuthPushTokenRefreshedSuccessfully()

    showcaseDataStoreFake.isFirebaseTokenUpdateNeeded = true
    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    verify(deviceClientMock).refreshMobileAuthPushToken(any(), any())
    assertThat(showcaseDataStoreFake.isFirebaseTokenUpdateNeeded).isFalse()
  }

  @Test
  fun `should not update refresh needed flag when refresh mobile auth push token failed`() {
    whenMobileAuthPushTokenRefreshFailed()

    showcaseDataStoreFake.isFirebaseTokenUpdateNeeded = true
    viewModel.onEvent(UiEvent.InitializeOneginiSdk)

    verify(deviceClientMock).refreshMobileAuthPushToken(any(), any())
    assertThat(showcaseDataStoreFake.isFirebaseTokenUpdateNeeded).isTrue()
  }

  @Test
  fun `should update selectedHandlers parameter successfully`() {
    assertThat(viewModel.uiState).isEqualTo(INITIAL_STATE)

    viewModel.onEvent(UiEvent.UpdateSelectedHandlers(emptyList()))

    val expectedState = INITIAL_STATE.copy(selectedHandlers = emptyList())
    assertThat(viewModel.uiState).isEqualTo(expectedState)
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

  private fun whenMobileAuthPushTokenRefreshedSuccessfully() {
    whenSdkInitializedSuccessfully()
    whenever(deviceClientMock.refreshMobileAuthPushToken(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiRefreshMobileAuthPushTokenHandler>(1).onSuccess()
    }
  }

  private fun whenMobileAuthPushTokenRefreshFailed() {
    whenSdkInitializedSuccessfully()
    whenever(deviceClientMock.refreshMobileAuthPushToken(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiRefreshMobileAuthPushTokenHandler>(1).onError(mock())
    }
  }

  companion object {
    private val INITIAL_STATE = SdkInitializationViewModel.State(
      shouldStoreCookies = true,
      httpConnectTimeout = null,
      httpReadTimeout = null,
      deviceConfigCacheDurationSeconds = null,
      isLoading = false,
      result = null,
      selectedHandlers = HandlerType.entries
    )
  }
}
