package com.onewelcome.showcaseapp.navigation

sealed class Screens(val route: String) {
  object Home : Screens("home_route")
  object Info : Screens("info_route")
  object SdkInitialization : Screens("sdk_initialization")
}
