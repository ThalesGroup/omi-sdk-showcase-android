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
  data object CreatePin : Screens("create_pin_${Constants.FULLSCREEN_PAGE}")
  data object AuthenticateWithPin : Screens("authenticate_with_pin_${Constants.FULLSCREEN_PAGE}")
  data object Pin : Screens("pin_route")
  data object MobileAuthentication: Screens("mobile_authentication")
  data object MobileAuthenticationEnrollment: Screens("mobile_authentication_enrollment")
  data object MobileAuthenticationPushEnrollment: Screens("mobile_authentication_push_enrollment")
}
