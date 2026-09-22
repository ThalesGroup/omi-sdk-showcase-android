package com.onewelcome.showcaseapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
// POC - START
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import android.util.Base64
import com.onewelcome.core.omisdk.handlers.DigiDRegistrationRequestHandler
// POC - END
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.theme.ShowcaseAppTheme
import com.onewelcome.core.util.Constants.MESSAGE_KEY
import com.onewelcome.core.util.Constants.PROFILE_ID_KEY
import com.onewelcome.core.util.Constants.TIMESTAMP_KEY
import com.onewelcome.core.util.Constants.TIME_TO_LIVE_SECONDS_KEY
import com.onewelcome.core.util.Constants.TRANSACTION_ID_KEY
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.navigation.ScreenHostContainer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowcaseActivity : FragmentActivity() {

  private val sharedPushViewModel: SharedPushViewModel by viewModels()

  // POC - START
  @Inject
  lateinit var digiDRegistrationRequestHandler : DigiDRegistrationRequestHandler
  // POC - END
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // POC - START
    handleDigiDReturn(intent)
    // POC - END
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
    // POC - START
    handleDigiDReturn(intent)
    // POC - END
    intent?.extras?.let { handlePushNotification(it) }
  }
  // POC - START
  private fun handleDigiDReturn(intent: Intent?) {
    val uri = intent?.data ?: return
    val isGenuflectoScheme = uri.scheme == "https" && uri.host == "genuflecto.github.io"
    if (isGenuflectoScheme) {
      val appApp = uri.getQueryParameter("app-app")
      appApp?.let {
        val decodedPart = decodeBase64(appApp.toString())
        if (decodedPart.isNotEmpty()) {
          digiDRegistrationRequestHandler.submitDigiDResponse(decodedPart)
        }
      }
    }
  }

  fun decodeBase64(base64String: String): String {
    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
    return String(decodedBytes, Charsets.UTF_8)
  }

  fun getFormattedIdToken(idToken: String): String {
    return try {
      val parts = idToken.split(".")
      if (parts.size >= 2) {
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
        val decodedJson = String(decodedBytes, Charsets.UTF_8)
        GsonBuilder()
          .setPrettyPrinting()
          .create()
          .toJson(JsonParser.parseString(decodedJson))
      } else {
        idToken
      }
    } catch (e: Exception) {
      idToken
    }
  }
  // POC - END
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
      sharedPushViewModel.onNewPush(pushRequest)
    }
  }
}
