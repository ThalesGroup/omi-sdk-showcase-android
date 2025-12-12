package com.onewelcome.core.omisdk

import android.content.Context
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.entity.TwoStepIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BiometricAuthenticationHandler
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithBiometricRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithOtpRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.omisdk.handlers.TwoStepRegistrationRequestHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val oneginiConfigModel: OneginiConfigModel,
  private val browserRegistrationRequestHandler: BrowserRegistrationRequestHandler,
  private val biometricAuthenticationHandler: BiometricAuthenticationHandler,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
  private val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler,
  private val mobileAuthWithBiometricRequestHandler: MobileAuthWithBiometricRequestHandler,
  private val mobileAuthWithOtpRequestHandler: MobileAuthWithOtpRequestHandler,
  private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler
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
        HandlerType.BIOMETRIC_AUTHENTICATION -> setBiometricAuthenticationRequestHandler(biometricAuthenticationHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH -> setMobileAuthWithPushRequestHandler(mobileAuthWithPushRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_OTP -> setMobileAuthWithOtpRequestHandler(mobileAuthWithOtpRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH_PIN -> setMobileAuthWithPushPinRequestHandler(mobileAuthWithPushPinRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH_BIOMETRIC -> setMobileAuthWithPushBiometricRequestHandler(mobileAuthWithBiometricRequestHandler)
          HandlerType.TWO_STEP_REGISTRATION -> setCustomIdentityProviders(setOf(TwoStepIdentityProvider(twoStepRegistrationRequestHandler)))
      }
    }
  }
}
