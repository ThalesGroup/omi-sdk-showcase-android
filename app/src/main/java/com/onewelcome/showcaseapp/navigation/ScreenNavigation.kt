package com.onewelcome.showcaseapp.navigation

sealed class ScreenNavigation(val route: String) {
  data object Home : ScreenNavigation("home_route")
  data object Info : ScreenNavigation("info_route")
  data object SdkInitialization : ScreenNavigation("sdk_initialization")
  data object OsCompatiblity : ScreenNavigation("os_compatibility")
}
