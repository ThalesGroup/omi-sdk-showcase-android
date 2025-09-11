package com.onewelcome.showcaseapp.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.onewelcome.showcaseapp.ShowcaseActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class PendingIntentFacadeImpl @Inject constructor(@ApplicationContext private val context: Context) : PendingIntentFacade {

  override fun getLaunchAppPendingIntent(notificationBundle: Bundle): PendingIntent {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.putExtras(notificationBundle)
    return PendingIntent.getActivity(context, Random.nextInt(), launchIntent, PendingIntent.FLAG_IMMUTABLE)
  }
}
