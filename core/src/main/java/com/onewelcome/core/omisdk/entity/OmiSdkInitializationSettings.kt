package com.onewelcome.core.omisdk.entity

import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?,
  val deviceConfigCacheDuration: Int?,
  val handlers: List<HandlerType>
  val browserRegistrationRequestHandler: BrowserRegistrationRequestHandler? = null,
  //TODO: Uncomment handlers when working on given authentication
//  val biometricAuthenticationRequestHandler: BiometricAuthenticationRequestHandler? = null,
//  val customAuthenticationRequestHandler: CustomAuthenticationRequestHandler? = null,
//  val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler? = null,
//  val mobileAuthWithPushPinRequestHandler: MobileAuthWithPushPinRequestHandler? = null,
//  val mobileAuthWithPushBiometricRequestHandler: MobileAuthWithPushBiometricRequestHandler? = null,
//  val mobileAuthWithPushCustomRequestHandler: MobileAuthWithPushCustomRequestHandler? = null,
//  val mobileAuthWithOtpRequestHandler: MobileAuthWithOtpRequestHandler? = null
)
