package com.onewelcome.showcaseapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onewelcome.internal.OsCompatibilityScreen
import com.onewelcome.showcaseapp.feature.home.HomeScreen
import com.onewelcome.showcaseapp.feature.info.InfoScreen
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationScreen
import com.onewelcome.showcaseapp.feature.userregistration.UserRegistrationScreen
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationScreen

@Composable
fun BottomNavigationBar() {
  val rootNavController = rememberNavController()
  val homeNavController = rememberNavController()
  val rootNavBackStackEntry by rootNavController.currentBackStackEntryAsState()
  val currentRootDestination = rootNavBackStackEntry?.destination
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar {
        BottomNavigationItem.getBottomNavigationItems(LocalContext.current).forEachIndexed { _, navigationItem ->
          NavigationBarItem(
            selected = navigationItem.route == currentRootDestination?.route,
            label = {
              Text(navigationItem.label)
            },
            icon = {
              Icon(
                navigationItem.icon,
                contentDescription = navigationItem.label
              )
            },
            onClick = {
              rootNavController.navigate(navigationItem.route) {
                launchSingleTop = true
                popUpTo(rootNavController.graph.startDestinationId) {
                  saveState = true
                }
                restoreState = true
              }
              if (navigationItem.route == currentRootDestination?.route && currentRootDestination.route == Screens.Home.route) {
                homeNavController.popBackStack(homeNavController.graph.startDestinationId, false)
              }
            }
          )
        }
      }
    }
  ) { paddingValues ->
    NavHost(
      navController = rootNavController,
      startDestination = Screens.Home.route,
      modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
      composable(Screens.Home.route) { HomeScreenNavHost(homeNavController) }
      composable(Screens.Info.route) { InfoScreen() }
      composable(Screens.OsCompatiblity.route) { OsCompatibilityScreen() }
    }
  }
}

@Composable
private fun HomeScreenNavHost(homeNavController: NavHostController) {
  NavHost(navController = homeNavController, startDestination = Screens.Home.route) {
    composable(Screens.Home.route) { HomeScreen(homeNavController) }
    composable(Screens.SdkInitialization.route) { SdkInitializationScreen(homeNavController) }
    composable(Screens.UserRegistration.route) { UserRegistrationScreen(homeNavController) }
    composable(Screens.BrowserRegistration.route) { BrowserRegistrationScreen(homeNavController) }
  }
}
