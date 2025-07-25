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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onewelcome.core.theme.isNotFullScreenRoute
import com.onewelcome.internal.OsCompatibilityScreen
import com.onewelcome.showcaseapp.feature.home.HomeScreen
import com.onewelcome.showcaseapp.feature.info.InfoScreen
import com.onewelcome.showcaseapp.feature.pin.AuthenticateWithPinViewModel
import com.onewelcome.showcaseapp.feature.pin.CreatePinViewModel
import com.onewelcome.showcaseapp.feature.mobileauth.MobileAuthenticationScreen
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentScreen
import com.onewelcome.showcaseapp.feature.pin.PinScreen
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationScreen
import com.onewelcome.showcaseapp.feature.userauthentication.UserAuthenticationScreen
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationScreen
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationScreen
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
      if (currentRootDestination?.route.isNotFullScreenRoute()) {
        ScreenWithNavBar(currentRootDestination, rootNavController, homeNavController)
      }
    }
  ) { paddingValues ->
    NavHost(
      navController = rootNavController,
      startDestination = Screens.Home.route,
      modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
      bottomNavigationScreens(homeNavController, rootNavController)
      pinFullScreenPages(rootNavController)
    }
  }
}

private fun NavGraphBuilder.pinFullScreenPages(rootNavController: NavHostController) {
  listOf(
    composable(Screens.AuthenticateWithPin.route) { AuthenticateWithPinScreen(rootNavController) },
    composable(Screens.CreatePin.route) { CreatePinScreen(rootNavController) },
  )
}

private fun NavGraphBuilder.bottomNavigationScreens(
  homeNavController: NavHostController,
  rootNavController: NavHostController
) {
  listOf(
    composable(Screens.Home.route) { HomeScreenNavHost(homeNavController, rootNavController) },
    composable(Screens.Info.route) { InfoScreen() },
    composable(Screens.OsCompatibility.route) { OsCompatibilityScreen() },
  )
}

@Composable
private fun CreatePinScreen(navController: NavController, viewModel: CreatePinViewModel = hiltViewModel()) {
  PinScreen(navController, viewModel)
}

@Composable
private fun AuthenticateWithPinScreen(navController: NavController, viewModel: AuthenticateWithPinViewModel = hiltViewModel()) {
  PinScreen(navController, viewModel)
}


@Composable
private fun ScreenWithNavBar(
  currentRootDestination: NavDestination?,
  rootNavController: NavHostController,
  homeNavController: NavHostController
) {
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

@Composable
private fun HomeScreenNavHost(homeNavController: NavHostController, rootNavController: NavHostController) {
  NavHost(navController = homeNavController, startDestination = Screens.Home.route) {
    composable(Screens.Home.route) { HomeScreen(homeNavController) }
    composable(Screens.SdkInitialization.route) { SdkInitializationScreen(homeNavController) }
    composable(Screens.UserRegistration.route) { UserRegistrationScreen(homeNavController) }
    composable(Screens.BrowserRegistration.route) { BrowserRegistrationScreen(homeNavController, rootNavController) }
    composable(Screens.UserAuthentication.route) { UserAuthenticationScreen(homeNavController) }
    composable(Screens.PinAuthentication.route) { PinAuthenticationScreen(homeNavController, rootNavController) }
    composable(Screens.UserDeregistration.route) { UserDeregistrationScreen(homeNavController) }
    composable(Screens.MobileAuthentication.route) { MobileAuthenticationScreen(homeNavController) }
    composable(Screens.MobileAuthenticationEnrollment.route) { MobileAuthenticationEnrollmentScreen(homeNavController) }
    composable(Screens.MobileAuthenticationPushEnrollment.route) { /*TODO AOSA-12*/ }
  }
}
