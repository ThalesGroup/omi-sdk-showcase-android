package com.onewelcome.showcaseapp.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

interface NotificationFacade {

  @RequiresApi(Build.VERSION_CODES.O)
  fun getMobileAuthNotificationChannel(): NotificationChannel

  fun getNotificationBuilder(channelId: String): NotificationCompat.Builder

  fun showNotification(notificationId: Int, notification: Notification)
}
