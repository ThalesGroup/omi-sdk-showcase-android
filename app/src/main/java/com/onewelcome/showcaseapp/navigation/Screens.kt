package com.onewelcome.showcaseapp.navigation

sealed class Screens(val route: String) {
  data object Home : Screens("home_route")
  data object Info : Screens("info_route")
  data object SdkInitialization : Screens("sdk_initialization")
  data object OsCompatiblity : Screens("os_compatibility")
  data object UserRegistration : Screens("user_registration")
  data object BrowserRegistration : Screens("browser_registration")
  data object Pin : Screens("pin_route")
  data object UserDeregistration: Screens("user_deregistration")
  data object MobileAuthentication: Screens("mobile_authentication")
  data object MobileAuthenticationEnrollment: Screens("mobile_authentication_enrollment")
  data object MobileAuthenticationPushEnrollment: Screens("mobile_authentication_push_enrollment")
}
