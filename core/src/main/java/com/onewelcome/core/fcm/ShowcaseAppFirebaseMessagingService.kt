package com.onewelcome.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.RefreshMobileAuthPushTokenUseCase
import com.onewelcome.data.datastore.ShowcaseDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowcaseAppFirebaseMessagingService : FirebaseMessagingService() {

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var refreshMobileAuthPushTokenUseCase: RefreshMobileAuthPushTokenUseCase

  @Inject
  lateinit var dataStore: ShowcaseDataStore

  override fun onNewToken(token: String) {
    CoroutineScope(Dispatchers.Default).launch {
      if (isSdkInitializedUseCase.execute()) {
        refreshMobileAuthPushTokenUseCase.execute(token)
      } else {
        dataStore.setFirebaseTokenUpdateNeeded(true)
      }
    }
  }
}
