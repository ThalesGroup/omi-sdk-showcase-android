package com.onewelcome.showcaseapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
  val label: String = "",
  val icon: ImageVector = Icons.Filled.Home,
  val route: String = ""
) {
  fun bottomNavigationItems(): List<BottomNavigationItem> {
    return listOf(
      BottomNavigationItem(
        label = "Home",
        icon = Icons.Filled.Home,
        route = Screens.Home.route
      ),
      BottomNavigationItem(
        label = "Info",
        icon = Icons.Filled.Info,
        route = Screens.Info.route
      ),
    )
  }
}
