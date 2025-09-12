package com.onewelcome.showcaseapp.viewmodel

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthenticationHandler
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.util.TestConstants
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class PushViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var authenticateWithPushUseCase: AuthenticateWithPushUseCase

  @Inject
  lateinit var oneginiClientMock: OneginiClient

  @Inject
  lateinit var omiSdkEngineFake: OmiSdkEngineFake

  private val userClientMock = mock<UserClient>()

  lateinit var viewModel: SharedPushViewModel

  val pushRequest = OneginiMobileAuthWithPushRequest("transactionId", "message", "userProfileId")

  @Before
  fun setup() {
    hiltRule.inject()
    mockSdkInitialized()
    whenever(oneginiClientMock.getUserClient()).thenReturn(userClientMock)

    viewModel = SharedPushViewModel(authenticateWithPushUseCase)
  }

  @Test
  fun `test`() {

    whenever(userClientMock.handleMobileAuthWithPushRequest(any(), any()))
      .thenAnswer { invocation -> invocation.getArgument<OneginiMobileAuthenticationHandler>(1).onSuccess(null) }

    viewModel.onNewPush(pushRequest)

    verify(viewModel.pushEvent)
  }


  private fun mockSdkInitialized() {
    omiSdkEngineFake.initialize(TestConstants.TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS)
    whenever(omiSdkEngineFake.oneginiClient).thenReturn(oneginiClientMock)
  }
}
