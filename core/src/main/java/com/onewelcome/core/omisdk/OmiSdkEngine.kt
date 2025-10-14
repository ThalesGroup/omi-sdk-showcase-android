package com.onewelcome.core.omisdk

import android.content.Context
import android.util.Log
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val oneginiConfigModel: OneginiConfigModel,
  private val browserRegistrationRequestHandler: BrowserRegistrationRequestHandler,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
) : OmiSdkFacade {

  override val oneginiClient
    get() = OneginiClient.instance ?: throw IllegalStateException("Onegini SDK instance not yet initialized")

  override fun initialize(settings: OmiSdkInitializationSettings): OneginiClient {
    return OneginiClientBuilder(context, createPinRequestHandler, pinAuthenticationRequestHandler)
      .setConfigModel(oneginiConfigModel)
      .shouldStoreCookies(settings.shouldStoreCookies)
      .apply {
        settings.httpConnectTimeout?.let { setHttpConnectTimeout(it) }
        settings.httpReadTimeout?.let { setHttpReadTimeout(it) }
        settings.deviceConfigCacheDuration?.let { setDeviceConfigCacheDurationSeconds(it) }
        setOptionalHandlers(settings)
      }.build()
  }

  private fun OneginiClientBuilder.setOptionalHandlers(settings: OmiSdkInitializationSettings) {
    settings.handlers.forEach {
      when (it) {
        HandlerType.BROWSER_REGISTRATION -> setBrowserRegistrationRequestHandler(browserRegistrationRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH -> setMobileAuthWithPushRequestHandler(mobileAuthWithPushRequestHandler)
      }
    }
  }
}
