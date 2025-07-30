package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTooltip
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PinAuthenticationScreen(
  navController: NavController,
  pinNavController: NavController,
  viewModel: PinAuthenticationViewModel = hiltViewModel()
) {
  PinAuthenticationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    navigationEvents = viewModel.navigationEvents,
    onNavigateToPinScreen = { pinNavController.navigate(Screens.AuthenticateWithPin.route) },
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
  LoadData(onEvent)
  SdkFeatureScreen(
    title = stringResource(R.string.pin_authentication),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.pin_authentication_description),
        link = Constants.DOCUMENTATION_PIN_AUTHENTICATION
      )
    },
    settings = { SettingSection(uiState, onEvent) },
    result = uiState.result?.let { { PinAuthenticationResult(it) } },
    action = { AuthenticationButton(uiState.isAuthenticateButtonEnabled, onEvent) }
  )
}

@Composable
fun LoadData(onEvent: (UiEvent) -> Unit) {
  LaunchedEffect(Unit) {
    onEvent.invoke(UiEvent.LoadData)
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
private fun SettingSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfiles, uiState.selectedUserProfile, onEvent)
    AuthenticatedProfileSection(uiState.authenticatedUserProfile)
  }
}

@Composable
fun AuthenticatedProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_authenticated_user_profile)
  )
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
private fun UserProfilesSection(userProfiles: Set<UserProfile>, selectedUserProfile: UserProfile?, onEvent: (UiEvent) -> Unit) {
  if (userProfiles.isEmpty()) {
    NoUserProfilesRegisteredSection()
  } else {
    UserProfileSelectionSection(selectedUserProfile, userProfiles, onEvent)
  }
}

@Composable
private fun NoUserProfilesRegisteredSection() {
  ShowcaseStatusCard(
    title = stringResource(R.string.user_profiles),
    description = stringResource(R.string.no_user_profiles),
    status = false,
    tooltipContent = { Text(stringResource(R.string.authentication_requirement_tooltip)) }
  )
}

@Composable
private fun UserProfileSelectionSection(
  selectedUserProfile: UserProfile?,
  userProfiles: Set<UserProfile>,
  onEvent: (UiEvent) -> Unit
) {
  ShowcaseCard {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          modifier = Modifier.weight(1f),
          text = stringResource(R.string.label_user_profile),
          style = MaterialTheme.typography.titleMedium
        )
        ShowcaseTooltip {
          Text(stringResource(R.string.authentication_choose_user_profile))
        }
      }
      userProfiles.forEach { userProfile ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent.invoke(UiEvent.UpdateSelectedUserProfile(userProfile)) }
        ) {
          RadioButton(
            selected = (userProfile == selectedUserProfile),
            onClick = { onEvent.invoke(UiEvent.UpdateSelectedUserProfile(userProfile)) }
          )
          Text(stringResource(R.string.user_profile_id, userProfile.profileId))
        }
      }
    }
  }
}

@Composable
private fun AuthenticationButton(isEnabled: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartPinAuthentication) },
    enabled = isEnabled,
  ) {
    Text(stringResource(R.string.authenticate))
  }
}

@Composable
private fun PinAuthenticationResult(result: Result<Pair<UserProfile, CustomInfo?>, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text(stringResource(R.string.authentication_successful))
          Text(stringResource(R.string.user_profile, it.first.profileId))
          Text(stringResource(R.string.custom_info, it.second.toString()))
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
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
