package com.onewelcome.showcaseapp.fcm

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.util.Constants
import com.onewelcome.core.util.Constants.NOTIFICATION_VIBRATION_PATTERN
import com.onewelcome.showcaseapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class NotificationSender @Inject constructor(
  @ApplicationContext private val context: Context,
  private val permissionsFacade: PermissionsFacade,
) {
  fun showNewTransactionNotification(notificationBundle: Bundle) {
    if (permissionsFacade.checkPostNotificationsPermission()) {
      NotificationManagerCompat.from(context).notify(MOBILE_AUTH_NOTIFICATION_ID, buildNewTransactionNotification(notificationBundle))
    }
  }

  private fun buildNewTransactionNotification(notificationBundle: Bundle) =
    NotificationCompat.Builder(context, Constants.MOBILE_AUTH_CHANNEL_ID)
      .setSmallIcon(R.drawable.notification_icon)
      .setContentTitle(R.string.new_pending_transaction)
      .setContentText(R.string.transaction_waiting_for_confirmation)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVibrate(NOTIFICATION_VIBRATION_PATTERN)
      .setAutoCancel(true)
      .setContentIntent(getLaunchAppPendingIntent(notificationBundle))
      .build()

  private fun getLaunchAppPendingIntent(notificationBundle: Bundle): PendingIntent {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.putExtras(notificationBundle)
    return PendingIntent.getActivity(context, Random.nextInt(), launchIntent, PendingIntent.FLAG_IMMUTABLE)
  }

  companion object {
    private const val MOBILE_AUTH_NOTIFICATION_ID = 100
  }
}
