package com.onewelcome.showcaseapp.fcm

import android.app.PendingIntent
import android.os.Bundle

interface PendingIntentFacade {

  fun getLaunchAppPendingIntent(notificationBundle: Bundle): PendingIntent
}
