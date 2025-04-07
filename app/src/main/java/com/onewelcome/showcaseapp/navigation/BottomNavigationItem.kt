package com.onewelcome.showcaseapp.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.onewelcome.showcaseapp.R

data class BottomNavigationItem(
  val label: String,
  val icon: ImageVector,
  val route: String,
) {
  companion object {
    fun getBottomNavigationItems(context: Context) = listOf(
      BottomNavigationItem(
        label = context.getString(R.string.home_screen_name),
        icon = Icons.Filled.Home,
        route = ScreenNavigation.Home.route
      ),
      BottomNavigationItem(
        label = context.getString(R.string.info_screen_name),
        icon = Icons.Filled.Info,
        route = ScreenNavigation.Info.route
      ),
    )
  }
}
