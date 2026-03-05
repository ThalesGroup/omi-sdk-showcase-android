package com.onewelcome.core.entity

import androidx.annotation.StringRes
import com.onewelcome.core.R

enum class HandlerType(@StringRes val title: Int) {
  BROWSER_REGISTRATION(R.string.handler_browser_registration),
  BIOMETRIC_AUTHENTICATION(R.string.handler_biometric_authentication),
  CUSTOM_AUTHENTICATION(R.string.handler_custom_authentication),
  MOBILE_AUTH_WITH_PUSH(R.string.handler_mobile_auth_with_push),
  MOBILE_AUTH_WITH_PUSH_PIN(R.string.handler_mobile_auth_with_push_and_pin),
  MOBILE_AUTH_WITH_PUSH_BIOMETRIC(R.string.handler_mobile_auth_with_push_and_biometrics),
  MOBILE_AUTH_WITH_PUSH_CUSTOM(R.string.handler_mobile_auth_with_push_and_custom),
  MOBILE_AUTH_WITH_OTP(R.string.handler_mobile_auth_with_otp),
  TWO_STEP_REGISTRATION(R.string.handler_two_step_registration)
}
