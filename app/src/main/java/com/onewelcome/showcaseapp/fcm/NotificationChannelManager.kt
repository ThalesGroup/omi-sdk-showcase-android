package com.onewelcome.showcaseapp.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.onewelcome.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationChannelManager @Inject constructor(@ApplicationContext private val context: Context) {
  fun registerNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      registerMobileAuthChannel()
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun registerMobileAuthChannel() {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    NotificationChannel(
      Constants.MOBILE_AUTH_CHANNEL_ID,
      "Transactions",
      NotificationManager.IMPORTANCE_HIGH,
    )
      .apply {
        enableLights(true)
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
      }
      .let { notificationManager.createNotificationChannel(it) }
  }
}
