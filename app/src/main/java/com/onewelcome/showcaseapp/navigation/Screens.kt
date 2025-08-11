package com.onewelcome.showcaseapp.navigation

import com.onewelcome.core.util.Constants

sealed class Screens(val route: String) {
  data object Home : Screens("home_route")
  data object Info : Screens("info_route")
  data object SdkInitialization : Screens("sdk_initialization")
  data object OsCompatibility : Screens("os_compatibility")
  data object UserRegistration : Screens("user_registration")
  data object BrowserRegistration : Screens("browser_registration")
  data object UserAuthentication : Screens("user_authentication")
  data object PinAuthentication : Screens("pin_authentication")
  data object UserDeregistration : Screens("user_deregistration")
  data object MobileAuthentication : Screens("mobile_authentication")
  data object MobileAuthenticationEnrollment : Screens("mobile_authentication_enrollment")
  data object MobileAuthenticationPushEnrollment : Screens("mobile_authentication_push_enrollment")
  data object ChangePin : Screens("change_pin")
  data object Logout : Screens("logout")
  data object PinAuthenticationInput : Screens("pin_authentication_input_${Constants.FULLSCREEN_PAGE}")
  data object CreatePinInput : Screens("create_pin_input_${Constants.FULLSCREEN_PAGE}")
}
