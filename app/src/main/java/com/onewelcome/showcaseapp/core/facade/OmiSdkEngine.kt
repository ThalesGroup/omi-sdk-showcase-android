package com.onewelcome.showcaseapp.core.facade

import android.content.Context
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onewelcome.showcaseapp.OneginiConfigModel
import com.onewelcome.showcaseapp.entity.OmiSdkInitializationSettings
import com.onewelcome.showcaseapp.omisdk.CreatePinRequestHandler
import com.onewelcome.showcaseapp.omisdk.PinAuthenticationRequestHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) : OmiSdkFacade {

  override val oneginiClient
    get() = OneginiClient.instance ?: throw IllegalStateException("Onegini SDK instance not yet initialized")

  override fun initialize(settings: OmiSdkInitializationSettings): OneginiClient {
    return OneginiClientBuilder(context, createPinRequestHandler, pinAuthenticationRequestHandler)
      .setConfigModel(OneginiConfigModel())
      .shouldStoreCookies(settings.shouldStoreCookies)
      .apply {
        settings.httpConnectTimeout?.let { setHttpConnectTimeout(it) }
        settings.httpReadTimeout?.let { setHttpReadTimeout(it) }
        settings.deviceConfigCacheDuration?.let { setDeviceConfigCacheDurationSeconds(it) }
      }.build()
  }
}
