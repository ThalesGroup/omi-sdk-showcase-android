package com.onewelcome.showcaseapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.theme.isNotFullScreenRoute
import com.onewelcome.core.util.Constants.MESSAGE_KEY
import com.onewelcome.core.util.Constants.PROFILE_ID_KEY
import com.onewelcome.core.util.Constants.TIMESTAMP_KEY
import com.onewelcome.core.util.Constants.TIME_TO_LIVE_SECONDS_KEY
import com.onewelcome.core.util.Constants.TRANSACTION_ID_KEY
import com.onewelcome.internal.OsCompatibilityScreen
import com.onewelcome.showcaseapp.PushViewModel
import com.onewelcome.showcaseapp.feature.changepin.ChangePinScreen
import com.onewelcome.showcaseapp.feature.home.HomeScreen
import com.onewelcome.showcaseapp.feature.info.InfoScreen
import com.onewelcome.showcaseapp.feature.logout.LogoutScreen
import com.onewelcome.showcaseapp.feature.mobileauth.MobileAuthenticationScreen
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentScreen
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentScreen
import com.onewelcome.showcaseapp.feature.pin.CreatePinInputViewModel
import com.onewelcome.showcaseapp.feature.pin.PinAuthenticationInputViewModel
import com.onewelcome.showcaseapp.feature.pin.PinScreen
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationScreen
import com.onewelcome.showcaseapp.feature.transaction.TransactionsScreen
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationResultScreen
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationScreen
import com.onewelcome.showcaseapp.feature.userauthentication.UserAuthenticationScreen
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationScreen
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationScreen
import com.onewelcome.showcaseapp.feature.userregistration.UserRegistrationScreen
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationScreen

@Composable
fun ScreenHostContainer() {
  val rootNavController = rememberNavController()
  val homeNavController = rememberNavController()
  val rootNavBackStackEntry by rootNavController.currentBackStackEntryAsState()
  val currentRootDestination = rootNavBackStackEntry?.destination
  ListenForPushEvents(rootNavController)
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

@Composable
private fun ListenForPushEvents(
  rootNavController: NavHostController,
  pushViewModel: PushViewModel = hiltViewModel(),
) {
  LaunchedEffect(Unit) {
    pushViewModel.pushEvent.collect {
      rootNavController.navigate("transaction_confirmation/${it.transactionId}/${it.message}/${it.userProfileId}/${it.timestamp}/${it.timeToLiveSeconds}")
    }
  }
}

private fun NavGraphBuilder.pinFullScreenPages(rootNavController: NavHostController) {
  composable(Screens.PinAuthenticationInput.route) { PinScreen(rootNavController, hiltViewModel<PinAuthenticationInputViewModel>()) }
  composable(Screens.CreatePinInput.route) { PinScreen(rootNavController, hiltViewModel<CreatePinInputViewModel>()) }
}

private fun NavGraphBuilder.bottomNavigationScreens(
  homeNavController: NavHostController,
  rootNavController: NavHostController,
) {
  composable(Screens.Home.route) { HomeScreenNavHost(homeNavController, rootNavController) }
  composable(Screens.Info.route) { InfoScreen() }
  composable(Screens.OsCompatibility.route) { OsCompatibilityScreen() }
  composable(Screens.Transactions.route) { TransactionsScreen() }
  navigation(startDestination = Screens.TransactionConfirmation.route, route = "sharedViewModelFlow") {
    composable(
      route = Screens.TransactionConfirmation.route,
      arguments = listOf(
        navArgument(TRANSACTION_ID_KEY) { type = NavType.StringType },
        navArgument(MESSAGE_KEY) { type = NavType.StringType },
        navArgument(PROFILE_ID_KEY) { type = NavType.StringType },
        navArgument(TIMESTAMP_KEY) { type = NavType.LongType },
        navArgument(TIME_TO_LIVE_SECONDS_KEY) { type = NavType.IntType },
      )
    ) { backStackEntry ->
      val transactionId = backStackEntry.arguments?.getString(TRANSACTION_ID_KEY) ?: ""
      val message = backStackEntry.arguments?.getString(MESSAGE_KEY) ?: ""
      val profileId = backStackEntry.arguments?.getString(PROFILE_ID_KEY) ?: ""
      val timestamp = backStackEntry.arguments?.getLong(TIMESTAMP_KEY) ?: 0L
      val timeToLiveSeconds = backStackEntry.arguments?.getInt(TIME_TO_LIVE_SECONDS_KEY) ?: 0
      val oneginiMobileAuthWithPushRequest =
        OneginiMobileAuthWithPushRequest(transactionId, message, profileId, timestamp, timeToLiveSeconds)

      val parentEntry = remember(backStackEntry) { rootNavController.getBackStackEntry("sharedViewModelFlow") }
      TransactionConfirmationScreen(rootNavController, oneginiMobileAuthWithPushRequest, hiltViewModel(parentEntry))
    }
    composable(Screens.TransactionConfirmationResult.route) { backStackEntry ->
      val parentEntry = remember(backStackEntry) { rootNavController.getBackStackEntry("sharedViewModelFlow") }
      TransactionConfirmationResultScreen(rootNavController, hiltViewModel(parentEntry))
    }
  }
}

@Composable
private fun ScreenWithNavBar(
  currentRootDestination: NavDestination?,
  rootNavController: NavHostController,
  homeNavController: NavHostController
) {
  NavigationBar {
    BottomNavigationBarItem.getBottomNavigationItems(LocalContext.current).forEachIndexed { _, navigationItem ->
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
    composable(Screens.MobileAuthenticationPushEnrollment.route) { MobileAuthenticationWithPushEnrollmentScreen(homeNavController) }
    composable(Screens.ChangePin.route) { ChangePinScreen(homeNavController, rootNavController) }
    composable(Screens.Logout.route) { LogoutScreen(homeNavController) }
  }
}
