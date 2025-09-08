package com.onewelcome.showcaseapp.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.onewelcome.showcaseapp.BuildConfig
import com.onewelcome.showcaseapp.R

data class BottomNavigationBarItem(
  val label: String,
  val icon: ImageVector,
  val route: String,
) {
  companion object {
    fun getBottomNavigationItems(context: Context): List<BottomNavigationBarItem> = buildList {
      add(
        BottomNavigationBarItem(
          label = context.getString(R.string.home_screen_name),
          icon = Icons.Filled.Home,
          route = Screens.Home.route
        )
      )
      add(
        BottomNavigationBarItem(
          label = context.getString(R.string.info_screen_name),
          icon = Icons.Filled.Info,
          route = Screens.Info.route
        )
      )
      add(
        BottomNavigationBarItem(
          label = context.getString(R.string.transactions_screen_name),
          icon = Icons.Filled.Notifications,
          route = Screens.Transactions.route
        )
      )
      if (BuildConfig.IS_INTERNAL_VARIANT) {
        add(
          BottomNavigationBarItem(
            label = context.getString(R.string.os_compatibility_screen_name),
            icon = Icons.Filled.Face,
            route = Screens.OsCompatibility.route
          )
        )
      }
    }
  }
}
