package com.onewelcome.core.omisdk

import android.content.Context
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) : OmiSdkFacade {

  override val oneginiClient
    get() = OneginiClient.Companion.instance ?: throw IllegalStateException("Onegini SDK instance not yet initialized")

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