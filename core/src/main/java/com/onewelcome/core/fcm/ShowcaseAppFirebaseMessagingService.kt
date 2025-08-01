package com.onewelcome.core.fcm

import com.github.michaelbull.result.flatMap
import com.google.firebase.messaging.FirebaseMessagingService
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import com.onewelcome.core.usecase.RefreshMobileAuthPushTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowcaseAppFirebaseMessagingService : FirebaseMessagingService() {

  @Inject
  lateinit var omiSdkInitializationUseCase: OmiSdkInitializationUseCase

  @Inject
  lateinit var refreshMobileAuthPushTokenUseCase: RefreshMobileAuthPushTokenUseCase

  override fun onNewToken(token: String) {
    CoroutineScope(Dispatchers.Default).launch {
      omiSdkInitializationUseCase.initialize(OmiSdkInitializationSettings.DEFAULT)
        .flatMap { refreshMobileAuthPushTokenUseCase.execute(token) }
    }
  }
}
