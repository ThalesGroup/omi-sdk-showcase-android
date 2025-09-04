package com.onewelcome.showcaseapp.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationFacadeImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val pushPermissionFacade: PermissionsFacade
) : NotificationFacade {

  @RequiresApi(Build.VERSION_CODES.O)
  override fun getMobileAuthNotificationChannel(): NotificationChannel {
    return NotificationChannel(
      Constants.MOBILE_AUTH_CHANNEL_ID,
      "Transactions",
      NotificationManager.IMPORTANCE_HIGH
    )
  }

  override fun getNotificationBuilder(channelId: String): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, channelId)
  }

  override fun showNotification(notificationId: Int, notification: Notification) {
    if (pushPermissionFacade.checkPostNotificationsPermission()) {
      NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
  }
}
