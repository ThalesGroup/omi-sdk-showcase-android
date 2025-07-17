package com.onewelcome.showcaseapp.navigation

sealed class Screens(val route: String) {
  data object Home : Screens("home_route")
  data object Info : Screens("info_route")
  data object SdkInitialization : Screens("sdk_initialization")
  data object OsCompatiblity : Screens("os_compatibility")
  data object UserRegistration : Screens("user_registration")
  data object BrowserRegistration : Screens("browser_registration")
  data object CreatePin : Screens("create_pin_route")
  data object AuthenticateWithPin : Screens("authenticate_with_pin_route")
  data object UserAuthentication : Screens("user_authentication")
  data object PinAuthentication : Screens("pin_authentication")
  data object UserDeregistration: Screens("user_deregistration")
}
