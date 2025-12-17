package com.onewelcome.core.entity

import androidx.annotation.StringRes
import com.onewelcome.core.R

//TODO: Uncomment handlers when working on given authentication
enum class HandlerType(@StringRes val title: Int) {
  BROWSER_REGISTRATION(R.string.handler_browser_registration),
   BIOMETRIC_AUTHENTICATION(R.string.handler_biometric_authentication),
//  CUSTOM_AUTHENTICATION,
  MOBILE_AUTH_WITH_PUSH(R.string.handler_mobile_auth_with_push),
  MOBILE_AUTH_WITH_PUSH_PIN(R.string.handler_mobile_auth_with_push_and_pin),
  //  MOBILE_AUTH_WITH_PUSH_BIOMETRIC,
//  MOBILE_AUTH_WITH_PUSH_CUSTOM,
  MOBILE_AUTH_WITH_OTP(R.string.handler_mobile_auth_with_otp)
}
