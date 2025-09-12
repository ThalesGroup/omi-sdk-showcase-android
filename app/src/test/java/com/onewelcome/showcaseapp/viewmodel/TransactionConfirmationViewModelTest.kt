package com.onewelcome.showcaseapp.viewmodel

import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class TransactionConfirmationViewModelTest {

  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler

  @Inject
  lateinit var authenticateWithPushUseCase: AuthenticateWithPushUseCase

  lateinit var viewModel: TransactionConfirmationViewModel

  fun setup() {
    hiltRule.inject()
    viewModel = TransactionConfirmationViewModel(mobileAuthWithPushRequestHandler, authenticateWithPushUseCase)
  }

  //accept
  //reject

  @Test
  fun `Given SDK is initialized, When Accept event is sent, Then State should be updated`() {
    viewModel.onEvent(TransactionConfirmationViewModel.UiEvent.Accept)


  }
}
