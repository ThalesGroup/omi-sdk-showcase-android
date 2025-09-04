package com.onewelcome.showcaseapp.fcm

import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.onewelcome.core.util.Constants
import javax.inject.Inject

class NotificationSender @Inject constructor(
  private val notificationFacade: NotificationFacade,
  private val pendingIntentFacade: PendingIntentFacade,
) {
  fun showNewTransactionNotification(notificationBundle: Bundle) {
    notificationFacade.showNotification(MOBILE_AUTH_NOTIFICATION_ID, buildNewTransactionNotification(notificationBundle))
  }

  private fun buildNewTransactionNotification(notificationBundle: Bundle) =
    notificationFacade.getNotificationBuilder(Constants.MOBILE_AUTH_CHANNEL_ID)
//      .setSmallIcon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_notification_overlay, null))
      .setContentTitle("New pending transaction")
      .setContentText("There is transaction waiting for confirmation")
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVibrate(longArrayOf(0, 500, 500, 500))
      .setAutoCancel(true)
      .setContentIntent(pendingIntentFacade.getLaunchAppPendingIntent(notificationBundle))
      .build()

  companion object {
    private const val MOBILE_AUTH_NOTIFICATION_ID = 100
  }
}
