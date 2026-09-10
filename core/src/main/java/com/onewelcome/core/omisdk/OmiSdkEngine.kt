package com.onewelcome.core.omisdk

import android.content.Context
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.CustomAuthenticator
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.entity.TwoStepIdentityProvider
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BiometricAuthenticationHandler
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.CustomAuthAuthenticationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthDeregistrationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthRegistrationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthenticationRequestHandler
//POC-START
import com.onewelcome.core.omisdk.entity.FidoAuthenticationIdentityProvider
import com.onewelcome.core.omisdk.entity.FidoIdentityProvider
import com.onewelcome.core.omisdk.entity.DigiDIdentityProvider
import com.onewelcome.core.omisdk.handlers.FidoAuthenticationRequestHandler
import com.onewelcome.core.omisdk.handlers.FidoRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.DigiDRegistrationRequestHandler
//POC-END
import com.onewelcome.core.omisdk.handlers.MobileAuthWithBiometricRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithOtpRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushCustomRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushPinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.omisdk.identityproviders.QrCodeIdentityProvider
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
  private val mobileAuthWithPushCustomRequestHandler: MobileAuthWithPushCustomRequestHandler,
  private val mobileAuthWithOtpRequestHandler: MobileAuthWithOtpRequestHandler,
  private val qrCodeIdentityProvider: QrCodeIdentityProvider,
  private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler,
  //POC-START
  private val fidoRegistrationRequestHandler: FidoRegistrationRequestHandler,
  private val fidoAuthenticationRequestHandler: FidoAuthenticationRequestHandler,
  private val digiDRegistrationRequestHandler: DigiDRegistrationRequestHandler,
  //POC-END
  private val customAuthRequestHandler: CustomAuthenticationRequestHandler,
  private val customAuthRegistrationAction: CustomAuthRegistrationAction,
  private val customAuthDeregistrationAction: CustomAuthDeregistrationAction,
  private val customAuthAuthenticationAction: CustomAuthAuthenticationAction
) : OmiSdkFacade {

  private var _isCustomAuthHandlerRegistered = false
  override val isCustomAuthHandlerRegistered: Boolean
    get() = _isCustomAuthHandlerRegistered

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
        addCustomIdentityProvider(qrCodeIdentityProvider)
      }.build()
  }

  private fun OneginiClientBuilder.setOptionalHandlers(settings: OmiSdkInitializationSettings) {
    //POC-START
    // Collect all custom identity providers first, then register them in a single call.
    // Multiple calls to setCustomIdentityProviders() would overwrite each other.
    val customIdentityProviders = mutableSetOf<com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider>()

    //POC-END
    settings.handlers.forEach {
      when (it) {
        HandlerType.BROWSER_REGISTRATION -> setBrowserRegistrationRequestHandler(browserRegistrationRequestHandler)
        HandlerType.BIOMETRIC_AUTHENTICATION -> setBiometricAuthenticationRequestHandler(biometricAuthenticationHandler)
        HandlerType.CUSTOM_AUTHENTICATION -> {
          val customAuthenticator = CustomAuthenticator(
            customAuthRegistrationAction,
            customAuthDeregistrationAction,
            customAuthAuthenticationAction
          )
          setCustomAuthenticators(setOf(customAuthenticator))
          setCustomAuthenticationRequestHandler(customAuthRequestHandler)
          _isCustomAuthHandlerRegistered = true
        }

        HandlerType.MOBILE_AUTH_WITH_PUSH -> setMobileAuthWithPushRequestHandler(mobileAuthWithPushRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_OTP -> setMobileAuthWithOtpRequestHandler(mobileAuthWithOtpRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH_PIN -> setMobileAuthWithPushPinRequestHandler(mobileAuthWithPushPinRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH_BIOMETRIC -> setMobileAuthWithPushBiometricRequestHandler(mobileAuthWithBiometricRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH_CUSTOM -> setMobileAuthWithPushCustomRequestHandler(mobileAuthWithPushCustomRequestHandler)
        HandlerType.TWO_STEP_REGISTRATION -> setCustomIdentityProviders(setOf(TwoStepIdentityProvider(twoStepRegistrationRequestHandler)))
        //POC-START
        HandlerType.FIDO_REGISTRATION -> customIdentityProviders.add(FidoIdentityProvider(fidoRegistrationRequestHandler))
        HandlerType.FIDO_AUTHENTICATION -> customIdentityProviders.add(FidoAuthenticationIdentityProvider(fidoAuthenticationRequestHandler))
        HandlerType.DIGID_APP2APP_REGISTRATION -> setCustomIdentityProviders(setOf(DigiDIdentityProvider(digiDRegistrationRequestHandler)))
        // POC - END
      }
    }

    //POC - START
    if (customIdentityProviders.isNotEmpty()) {
      setCustomIdentityProviders(customIdentityProviders)
    }
    //POC-END
  }
}
