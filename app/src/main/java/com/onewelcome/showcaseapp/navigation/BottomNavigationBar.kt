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
import com.onewelcome.showcaseapp.ui.screens.HomeScreen
import com.onewelcome.showcaseapp.ui.screens.InfoScreen
import com.onewelcome.showcaseapp.ui.screens.sections.SdkInitializationScreen

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
              if (navigationItem.route == currentRootDestination?.route && currentRootDestination.route == ScreenNavigation.Home.route) {
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
      startDestination = ScreenNavigation.Home.route,
      modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
      composable(ScreenNavigation.Home.route) { HomeScreenNavHost(homeNavController) }
      composable(ScreenNavigation.Info.route) { InfoScreen() }
    }
  }
}

@Composable
private fun HomeScreenNavHost(homeNavController: NavHostController) {
  NavHost(navController = homeNavController, startDestination = ScreenNavigation.Home.route) {
    composable(ScreenNavigation.Home.route) { HomeScreen(homeNavController) }
    composable(ScreenNavigation.SdkInitialization.route) { SdkInitializationScreen(homeNavController) }
  }
}
