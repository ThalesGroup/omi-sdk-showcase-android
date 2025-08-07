package com.onewelcome.showcaseapp.feature.changepin

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
import androidx.navigation.NavHostController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.changepin.ChangePinViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.changepin.ChangePinViewModel.State
import com.onewelcome.showcaseapp.feature.changepin.ChangePinViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ChangePinScreen(
  homeNavController: NavHostController,
  pinNavController: NavHostController,
  viewModel: ChangePinViewModel = hiltViewModel()
) {
  ChangePinScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { homeNavController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    navigationEvents = viewModel.navigationEvents,
    onNavigateToPinScreen = { pinNavController.navigate(Screens.ChangePinInput.route) },
  )
}

@Composable
fun ChangePinScreenContent(
  onNavigateBack: () -> Unit,
  uiState: State,
  onEvent: (UiEvent) -> Unit,
  navigationEvents: Flow<NavigationEvent>,
  onNavigateToPinScreen: () -> Unit,
) {
  ListenForPinNavigationEvent(navigationEvents, onNavigateToPinScreen)
  SdkFeatureScreen(
    title = stringResource(R.string.change_pin),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.change_pin_description),
        link = Constants.DOCUMENTATION_CHANGE_PIN
      )
    },
    settings = { SettingSection(isSdkInitialized = uiState.isSdkInitialized, authenticatedUserProfile = uiState.authenticatedUserProfile) },
    result = uiState.result?.let { { PinChangeResult(it) } },
    action = { ChangePinButton(onEvent) }
  )
}

@Composable
fun ChangePinButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartPinChange) },
  ) {
    Text(stringResource(R.string.change_pin))
  }
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
private fun SettingSection(isSdkInitialized: Boolean, authenticatedUserProfile: UserProfile?) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(isSdkInitialized)
    AuthenticatedProfileSection(authenticatedUserProfile)
  }
}

@Composable
private fun AuthenticatedProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_authenticated_user_profile),
    tooltipContent = { Text(stringResource(R.string.user_needs_to_be_authenticated_to_perform_pin_change)) }
  )
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_pin_change)) }
  )
}

@Composable
private fun PinChangeResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text("Amazing success")
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  ChangePinScreenContent(
    uiState = State(),
    onEvent = {},
    onNavigateBack = {},
    navigationEvents = emptyFlow(),
    onNavigateToPinScreen = {}
  )
}
