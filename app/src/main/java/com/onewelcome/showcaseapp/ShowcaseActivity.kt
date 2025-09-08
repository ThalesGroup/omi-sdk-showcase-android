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
import com.onewelcome.core.util.Constants.TRANSACTION_ID_KEY
import com.onewelcome.showcaseapp.navigation.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowcaseActivity : ComponentActivity() {

  private val pushNavigationViewModel: PushNavigationViewModel by viewModels()

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handlePushNotification(intent.extras)
  }

  private fun handlePushNotification(extras: Bundle?) {
    extras?.let { data ->
      val pushData = OneginiMobileAuthWithPushRequest(
        data.getString(TRANSACTION_ID_KEY) ?: "",
        data.getString(MESSAGE_KEY) ?: "",
        data.getString(PROFILE_ID_KEY) ?: ""
      )
      pushNavigationViewModel.onNewPush(pushData)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ShowcaseAppTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          BottomNavigationBar()
        }
      }
    }
  }
}
