package com.onewelcome.showcaseapp

import android.app.Application
import android.util.Log
import com.onewelcome.core.facade.SdkAutoInitializationFacadeImpl
import com.onewelcome.showcaseapp.fcm.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ShowcaseApplication : Application() {

  @Inject
  lateinit var autoInitializationManager: SdkAutoInitializationFacadeImpl

  @Inject
  lateinit var notificationChannelManager: NotificationChannelManager

  override fun onCreate() {
    super.onCreate()
    autoInitializeSdk()
    notificationChannelManager.registerNotificationChannels()
  }

  private fun autoInitializeSdk() {
    CoroutineScope(Dispatchers.IO).launch {
      autoInitializationManager.execute()
    }
  }
}
