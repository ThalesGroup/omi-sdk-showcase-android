package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PinAuthenticationScreen(
  navController: NavController,
  viewModel: PinAuthenticationViewModel = hiltViewModel()
) {
  PinAuthenticationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    navigationEvents = viewModel.navigationEvents,
    onNavigateToPinScreen = { navController.navigate(Screens.AuthenticateWithPin.route) },
  )
}

@Composable
private fun PinAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  uiState: State,
  onEvent: (UiEvent) -> Unit,
  navigationEvents: Flow<NavigationEvent>,
  onNavigateToPinScreen: () -> Unit,
) {
  ListenForPinNavigationEvent(navigationEvents, onNavigateToPinScreen)
  SdkFeatureScreen(
    title = stringResource(R.string.pin_authentication),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = "Add some nice description",
        link = "add a link"
      )
    },
    settings = { SettingSection(uiState) },
    result = uiState.result?.let { { PinAuthenticationResult(it) } },
    action = {
      AuthenticationButton(onEvent)
      CancellationButton(uiState.isAuthenticationCancellationEnabled, onEvent)
    }
  )
}

@Composable
private fun ListenForPinNavigationEvent(
  navigationEvents: Flow<NavigationEvent>,
  onNavigateToPinScreen: () -> Unit
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.ToPinScreen -> onNavigateToPinScreen.invoke()
      }
    }
  }
}

@Composable
private fun SettingSection(uiState: State) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfileIds)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_registration)) }
  )
}

@Composable
private fun UserProfilesSection(userProfiles: List<String>) {
  val text = getUserProfilesText(userProfiles)
  UserProfilesCard(text)
}

@Composable
private fun getUserProfilesText(userProfiles: List<String>): String {
  return if (userProfiles.isNotEmpty()) {
    userProfiles.separateItemsWithComa()
  } else {
    stringResource(R.string.no_user_profiles)
  }
}

@Composable
private fun UserProfilesCard(userProfiles: String) {
  ShowcaseStatusCard(
    title = stringResource(R.string.user_profiles),
    description = userProfiles,
    tooltipContent = { Text(stringResource(R.string.user_needs_to_be_registered_to_perform_authentication)) }
  )
}

@Composable
private fun AuthenticationButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartPinAuthentication) }
  ) {
    Text(stringResource(R.string.authenticate))
  }
}

@Composable
private fun CancellationButton(isRegistrationCancellationEnabled: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.CancelAuthentication) },
    enabled = isRegistrationCancellationEnabled
  ) {
    Text(stringResource(R.string.cancel_registration))
  }
}

@Composable
private fun PinAuthenticationResult(result: Result<Pair<UserProfile, CustomInfo?>, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text("magnificent success")
        }
      }
      .onFailure { Text("$it") }
  }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  PinAuthenticationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {},
    navigationEvents = emptyFlow(),
    onNavigateToPinScreen = {}
  )
}
