package com.onewelcome.showcaseapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.theme.ShowcaseAppTheme
import com.onewelcome.core.util.Constants.MESSAGE_KEY
import com.onewelcome.core.util.Constants.PROFILE_ID_KEY
import com.onewelcome.core.util.Constants.TIMESTAMP_KEY
import com.onewelcome.core.util.Constants.TIME_TO_LIVE_SECONDS_KEY
import com.onewelcome.core.util.Constants.TRANSACTION_ID_KEY
import com.onewelcome.showcaseapp.navigation.ScreenHostContainer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowcaseActivity : ComponentActivity() {

  private val pushViewModel: PushViewModel by viewModels()

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    intent.extras?.let { handlePushNotification(it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent()
    enableEdgeToEdge()
    setContent {
      ShowcaseAppTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ScreenHostContainer()
        }
      }
    }
  }

  private fun handleIntent() {
    intent?.extras?.let { handlePushNotification(it) }
  }

  private fun handlePushNotification(extras: Bundle) {
    val transactionId = extras.getString(TRANSACTION_ID_KEY)
    val message = extras.getString(MESSAGE_KEY)
    val profileId = extras.getString(PROFILE_ID_KEY)
    val isValidPushNotification = transactionId != null && message != null && profileId != null
    if (isValidPushNotification) {
      val pushRequest = OneginiMobileAuthWithPushRequest(
        transactionId,
        message,
        profileId,
        extras.getLong(TIMESTAMP_KEY),
        extras.getInt(TIME_TO_LIVE_SECONDS_KEY)
      )
      pushViewModel.onNewPush(pushRequest)
    }
  }
}
