package com.onewelcome.showcaseapp.feature.sdkinitialization

//TODO: Uncomment handlers when working on given authentication
enum class HandlerType {
  BROWSER_REGISTRATION,
//  BIOMETRIC_AUTHENTICATION,
//  CUSTOM_AUTHENTICATION,
//  MOBILE_AUTH_WITH_PUSH,
//  MOBILE_AUTH_WITH_PUSH_PIN,
//  MOBILE_AUTH_WITH_PUSH_BIOMETRIC,
//  MOBILE_AUTH_WITH_PUSH_CUSTOM,
//  MOBILE_AUTH_WITH_OTP
}

val HandlerType.displayName: String
  get() = name
    .lowercase()
    .split('_')
    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
