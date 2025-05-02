package com.onewelcome.showcaseapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.onewelcome.core.omisdk.OmiSdkEngine
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.theme.ShowcaseAppTheme
import com.onewelcome.showcaseapp.navigation.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowcaseActivity : ComponentActivity() {

  @Inject
  lateinit var omiSdkEngine: OmiSdkEngine

  @Inject
  lateinit var browserRegistrationRequestHandler: BrowserRegistrationRequestHandler

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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val uri = intent.data
    val scheme = uri?.scheme
    val isBrowserRegistrationRedirect =
      uri != null && scheme != null && omiSdkEngine.oneginiClient.configModel.redirectUri.startsWith(scheme)
    if (isBrowserRegistrationRedirect) {
      browserRegistrationRequestHandler.handleRegistrationCallback(uri)
    }
  }
}
