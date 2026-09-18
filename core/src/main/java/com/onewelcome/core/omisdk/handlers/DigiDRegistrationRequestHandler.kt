// POC - START
package com.onewelcome.core.omisdk.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomTwoStepRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigiDRegistrationRequestHandler @Inject constructor(
  @ApplicationContext val applicationContext: Context
) : OneginiCustomTwoStepRegistrationAction {
  companion object {
    private const val REDIRECT_URI = "https://genuflecto.github.io/owshowcase"
  }

  private var finishCallback: OneginiCustomRegistrationCallback? = null
  private var sessionState: String = ""

  private val _authenticationUrlFlow = MutableSharedFlow<DigiDStepData>(replay = 1)
  val authenticationUrlFlow: SharedFlow<DigiDStepData> = _authenticationUrlFlow.asSharedFlow()

  override fun initRegistration(
    registrationCallback: OneginiCustomRegistrationCallback,
    customInfo: CustomInfo?
  ) {
    sessionState = UUID.randomUUID().toString()
    val initData = JSONObject().apply {
      put("state", sessionState)
      put("redirectURI", REDIRECT_URI)
    }.toString()
    registrationCallback.returnSuccess(initData)
  }

  override fun finishRegistration(
    callback: OneginiCustomRegistrationCallback,
    customInfo: CustomInfo?
  ) {
    finishCallback = callback
    val authUrl = customInfo?.data ?: ""

    val parsedUrl = try {
      JSONObject(authUrl).getString("url")
    } catch (e: Exception) {
      authUrl
    }
    // Working Code
    Intent(Intent.ACTION_VIEW, Uri.parse(parsedUrl)).apply {
      // We added the line below to explicitly target the DigiD pre-production app:
      setPackage("nl.rijksoverheid.digid.pp")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
      }
    }.let { applicationContext.startActivity(it) }
    _authenticationUrlFlow.tryEmit(DigiDStepData(authenticationUrl = parsedUrl, sessionState = sessionState))
  }

  fun submitDigiDResponse(samlArt: String) {
    finishCallback?.returnSuccess(samlArt)
    //cleanUp()
  }

  fun cancelRegistration() {
    finishCallback?.returnError(Exception("DigiD registration canceled by user"))
    cleanUp()
  }

  fun isRegistrationInProgress(): Boolean = finishCallback != null

  private fun cleanUp() {
    finishCallback = null
    _authenticationUrlFlow.resetReplayCache()
  }

  data class DigiDStepData(
    val authenticationUrl: String,
    val sessionState: String
  )
}
// POC - END
