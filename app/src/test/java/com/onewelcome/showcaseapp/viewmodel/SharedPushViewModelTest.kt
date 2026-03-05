package com.onewelcome.showcaseapp.viewmodel

import androidx.biometric.BiometricPrompt.CryptoObject
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiAcceptDenyCallback
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBiometricCallback
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.manager.SdkAutoInitializationManager
import com.onewelcome.core.notification.NotificationEventDispatcher
import com.onewelcome.core.omisdk.handlers.MobileAuthWithBiometricRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.core.util.TestConstants.TEST_CUSTOM_INFO
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.fakes.PreferencesManagerFake
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.utils.withEqualsForThrowable
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class SharedPushViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler

  @Inject
  lateinit var authenticateWithPushUseCase: AuthenticateWithPushUseCase

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  @Inject
  lateinit var oneginiClient: OneginiClient

  @Inject
  lateinit var notificationEventDispatcher: NotificationEventDispatcher

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var preferencesManager: PreferencesManagerFake

  @Inject
  lateinit var sdkAutoInitializationManager: SdkAutoInitializationManager

  @Inject
  lateinit var mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler

  @Inject
  lateinit var mobileAuthWithBiometricRequestHandler: MobileAuthWithBiometricRequestHandler

  lateinit var viewModel: SharedPushViewModel

  private val userClientMock: UserClient = mock()
  private val acceptDenyCallback: OneginiAcceptDenyCallback = mock()
  private val pinCallback: OneginiPinCallback = mock()
  private val cryptoObject: CryptoObject = mock()
  private val biometricCallback: OneginiBiometricCallback = mock()
  private val mockOneginiMobileAuthenticationError: OneginiMobileAuthenticationError = mock()
  private val pushRequest = OneginiMobileAuthWithPushRequest("transactionId", "message", "userProfileId")
  private val initError: OneginiInitializationError = mock()
  private val initialState = SharedPushViewModel.UiState(pushRequest = null, result = null)

  @Before
  fun setup() {
    hiltRule.inject()
    viewModel = SharedPushViewModel(
      authenticateWithPushUseCase,
      mobileAuthWithPushRequestHandler,
      notificationEventDispatcher,
      mobileAuthWithPushPinRequestHandler,
      isSdkInitializedUseCase,
      preferencesManager,
      sdkAutoInitializationManager,
      mobileAuthWithBiometricRequestHandler,
    )
  }

  // Original Tests
  @Test
  fun `When viewmodel is initialized, Then default state should be returned`() {
    val expectedState = initialState

    assertThat(viewModel.uiState).isEqualTo(expectedState)
  }

  @Test
  fun `When new push is sent, Then state should be updated`() {
    mockSdkInitialized()

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(initialState.copy(pushRequest = pushRequest))
  }

  @Test
  fun `When new push is sent and authentication is successful and returns null, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onSuccess(null)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(viewModel.uiState.copy(pushRequest = pushRequest, result = Ok(null)))
  }

  @Test
  fun `When new push is sent and authentication is successful and returns Custom Info, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onSuccess(TEST_CUSTOM_INFO)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(viewModel.uiState.copy(pushRequest = pushRequest, result = Ok(TEST_CUSTOM_INFO)))
  }

  @Test
  fun `When new push is sent and authentication failed, Then state should be updated`() {
    mockSdkInitialized()
    mockUserClient()

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any())).thenAnswer { invocation ->
      invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onError(mockOneginiMobileAuthenticationError)
    }

    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState).isEqualTo(
      viewModel.uiState.copy(
        pushRequest = pushRequest,
        result = Err(mockOneginiMobileAuthenticationError)
      )
    )
  }

  @Test
  fun `Given SDK is not initialized and it is not auto initialized, When new push is sent, Then state should be updated`() {
    runTest {
      preferencesManager.setSdkAutoInitializationEnabled(false)
    }
    viewModel.onNewPush(pushRequest)
    val expectedState = viewModel.uiState.copy(
      pushRequest = pushRequest,
      result = Err(IllegalStateException("SDK needs to be initialized to handle push transactions"))
    )

    assertThat(viewModel.uiState).usingRecursiveComparison().withEqualsForThrowable().isEqualTo(expectedState)
  }

  @Test
  fun `When push request handler emits, Then should navigate to transaction confirmation and update state`() {
    mockSdkInitialized()

    mobileAuthWithPushRequestHandler
      .startAuthentication(TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST, acceptDenyCallback)

    assertThat(viewModel.uiState.pushType).isEqualTo(SharedPushViewModel.PushType.PUSH)
    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionConfirmationScreen)
    }
  }

  @Test
  fun `When push pin handler emits, Then should navigate to transaction confirmation and update state`() {
    mockSdkInitialized()

    mobileAuthWithPushPinRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      pinCallback,
      TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER,
      null
    )

    assertThat(viewModel.uiState.pushType).isEqualTo(SharedPushViewModel.PushType.PUSH_PIN)

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionConfirmationScreen)
    }
  }

  @Test
  fun `When biometric handler emits, Then should navigate to transaction confirmation with crypto object`() {
    mockSdkInitialized()

    mobileAuthWithBiometricRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      cryptoObject,
      biometricCallback
    )

    assertThat(viewModel.uiState.pushType).isEqualTo(SharedPushViewModel.PushType.PUSH_BIOMETRIC)
    assertThat(viewModel.uiState.cryptoObject).isEqualTo(cryptoObject)
    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionConfirmationScreen)
    }
  }

  @Test
  fun `When authentication event dispatched with success, Then should navigate to result screen`() {
    mockSdkInitialized()

    notificationEventDispatcher.send(Ok(TEST_CUSTOM_INFO))

    assertThat(viewModel.uiState.result).isEqualTo(Ok(TEST_CUSTOM_INFO))
    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionResultScreen)
    }
  }

  @Test
  fun `When authentication event dispatched with error, Then should navigate to result screen`() {
    mockSdkInitialized()

    notificationEventDispatcher.send(Err(mockOneginiMobileAuthenticationError))

    assertThat(viewModel.uiState.result)
      .usingRecursiveComparison()
      .withEqualsForThrowable()
      .isEqualTo(Err(mockOneginiMobileAuthenticationError))
    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionResultScreen)
    }
  }

  @Test
  fun `Given PUSH_PIN type, When Accept event sent, Then should navigate to pin confirmation`() {
    mockSdkInitialized()

    mobileAuthWithPushPinRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      pinCallback,
      TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER,
      null
    )

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionConfirmationScreen)
    }

    viewModel.onEvent(SharedPushViewModel.UiEvent.Accept)

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToPinConfirmationScreen)
    }
  }

  @Test
  fun `Given PUSH_BIOMETRIC type with crypto object, When Accept event sent, Then should show biometric prompt`() {
    mockSdkInitialized()

    mobileAuthWithBiometricRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      cryptoObject,
      biometricCallback
    )

    runTest {
      assertThat(viewModel.navigationEvents.first()).isEqualTo(SharedPushViewModel.NavigationEvent.NavigateToTransactionConfirmationScreen)
    }

    viewModel.onEvent(SharedPushViewModel.UiEvent.Accept)

    runTest {
      assertThat(viewModel.biometricEvents.first()).isEqualTo(SharedPushViewModel.BiometricEvent.ShowBiometricPrompt(cryptoObject))
    }
  }

  @Test
  fun `Given PUSH type, When Accept event sent, Then should call acceptAuthenticationRequest`() {
    mockSdkInitialized()

    mobileAuthWithPushRequestHandler.startAuthentication(TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST, acceptDenyCallback)

    viewModel.onEvent(SharedPushViewModel.UiEvent.Accept)

    verify(acceptDenyCallback).acceptAuthenticationRequest()
  }

  @Test
  fun `Given PUSH type, When Reject event sent, Then should call denyAuthenticationRequest`() {
    mockSdkInitialized()

    mobileAuthWithPushRequestHandler.startAuthentication(TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST, acceptDenyCallback)

    viewModel.onEvent(SharedPushViewModel.UiEvent.Reject)

    verify(acceptDenyCallback).denyAuthenticationRequest()
  }

  @Test
  fun `Given PUSH_PIN type, When Reject event sent, Then should call pin callback deny`() {
    mockSdkInitialized()

    mobileAuthWithPushPinRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      pinCallback,
      TestConstants.TEST_AUTHENTICATION_ATTEMPT_COUNTER,
      null
    )

    viewModel.onEvent(SharedPushViewModel.UiEvent.Reject)

    verify(pinCallback).denyAuthenticationRequest()
  }

  @Test
  fun `Given PUSH_BIOMETRIC type, When Reject event sent, Then should call biometric callback deny`() {
    val cryptoObject: CryptoObject = mock()
    val callback: OneginiBiometricCallback = mock()
    mockSdkInitialized()

    mobileAuthWithBiometricRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      cryptoObject,
      callback
    )

    viewModel.onEvent(SharedPushViewModel.UiEvent.Reject)

    verify(callback).denyAuthenticationRequest()
  }

  @Test
  fun `When AcceptBiometric event sent, Then should call biometric callback success`() {
    mockSdkInitialized()

    mobileAuthWithBiometricRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      cryptoObject,
      biometricCallback
    )

    viewModel.onEvent(SharedPushViewModel.UiEvent.AcceptBiometric)

    verify(biometricCallback).userAuthenticatedSuccessfully()
  }

  @Test
  fun `When DeclineBiometric event sent, Then should call biometric callback error with error code`() {
    mockSdkInitialized()

    mobileAuthWithBiometricRequestHandler.startAuthentication(
      TestConstants.TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST,
      cryptoObject,
      biometricCallback
    )

    viewModel.onEvent(SharedPushViewModel.UiEvent.DeclineBiometric(10000))

    verify(biometricCallback).onBiometricAuthenticationError(10000)
  }


  @Test
  fun `Given SDK auto-init enabled and fails, When new push sent, Then should show error`() {
    runTest {
      preferencesManager.setSdkAutoInitializationEnabled(true)
      val deferredResult = async {
        Err(initError)
      }
      sdkAutoInitializationManager.deferredResult = deferredResult
    }
    viewModel.onNewPush(pushRequest)

    assertThat(viewModel.uiState.pushRequest).isEqualTo(pushRequest)
    assertThat(viewModel.uiState.result).usingRecursiveComparison().withEqualsForThrowable().isEqualTo(Err(initError))
  }

  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClient)
  }

  private fun mockUserClient() {
    whenever(oneginiClient.getUserClient()).thenReturn(userClientMock)
  }

  companion object {
  }
}
