package com.onewelcome.core.fcm

import com.github.michaelbull.result.map
import com.google.firebase.messaging.FirebaseMessagingService
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationWithPushUseCase
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
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
  lateinit var enrollForMobileAuthenticationWithPushUseCase: EnrollForMobileAuthenticationWithPushUseCase

  override fun onNewToken(token: String) {
    CoroutineScope(Dispatchers.Default).launch {
      omiSdkInitializationUseCase.initialize(OmiSdkInitializationSettings.DEFAULT)
        .map { enrollForMobileAuthenticationWithPushUseCase.execute(token) }
    }
  }
}
