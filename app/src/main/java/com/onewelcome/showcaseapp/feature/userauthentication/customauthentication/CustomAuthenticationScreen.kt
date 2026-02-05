package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.navigation.NavHostController
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
import com.onewelcome.showcaseapp.feature.userauthentication.customauthentication.CustomAuthenticationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userauthentication.customauthentication.CustomAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.customauthentication.CustomAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CustomAuthenticationScreen(
  homeNavController: NavHostController,
  pinNavController: NavController,
  viewModel: CustomAuthenticationViewModel = hiltViewModel()
) {
  CustomAuthenticationContent(
    uiState = viewModel.uiState,
    navigationEvents = viewModel.navigationEvents,
    onEvent = { viewModel.onEvent(it) },
    onNavigateBack = { homeNavController.popBackStack() },
    onNavigateToPinScreen = { pinNavController.navigate(Screens.PinAuthenticationInput.route) },
    onNavigateToSdkInitialization = { homeNavController.navigate(Screens.SdkInitialization.route) },
  )
}

@Composable
private fun CustomAuthenticationContent(
  uiState: State,
  navigationEvents: Flow<NavigationEvent>,
  onEvent: (UiEvent) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateToPinScreen: () -> Unit,
  onNavigateToSdkInitialization: () -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_custom_authentication),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.custom_authentication_description),
        link = Constants.DOCUMENTATION_CUSTOM_AUTHENTICATION
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { CustomAuthenticationResult(it) } },
    action = { AuthenticationButton(uiState.isAuthenticateButtonEnabled, uiState.isLoading, onEvent) }
  )
  ListenForNavigationEvents(navigationEvents, onNavigateToPinScreen, onNavigateToSdkInitialization)
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    // Show warning if custom auth handler is not registered
    if (!uiState.isCustomAuthHandlerRegistered && uiState.isSdkInitialized) {
      CustomAuthHandlerNotRegisteredWarning(onEvent)
    }
    SdkInitializationSection(uiState.isSdkInitialized)
    CustomAuthHandlerRegisteredSection(uiState.isCustomAuthHandlerRegistered)
    UserProfilesSection(uiState.userProfiles, uiState.selectedUserProfile, onEvent)
    CustomAuthenticatorStatusSection(uiState.isCustomAuthenticatorRegisteredForUser)
    AuthenticatedProfileSection(uiState.authenticatedUserProfile)
  }
}

@Composable
private fun CustomAuthHandlerNotRegisteredWarning(onEvent: (UiEvent) -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer
    )
  ) {
    Column(
      modifier = Modifier.padding(Dimensions.sPadding)
    ) {
      Text(
        text = stringResource(R.string.custom_auth_handler_not_registered_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onErrorContainer
      )
      Spacer(modifier = Modifier.height(Dimensions.verticalSpacing))
      Text(
        text = stringResource(R.string.custom_auth_handler_not_registered_message),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer
      )
      Spacer(modifier = Modifier.height(Dimensions.verticalSpacing))
      OutlinedButton(
        onClick = { onEvent(UiEvent.NavigateToSdkInitialization) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
      ) {
        Text(stringResource(R.string.go_to_sdk_initialization))
      }
    }
  }
}

@Composable
private fun CustomAuthHandlerRegisteredSection(isCustomAuthHandlerRegistered: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.custom_auth_handler_status_title),
    status = isCustomAuthHandlerRegistered,
    tooltipContent = { Text(stringResource(R.string.custom_auth_handler_requirement_tooltip)) }
  )
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_authentication)) }
  )
}

@Composable
private fun UserProfilesSection(
  userProfiles: Set<UserProfile>,
  selectedUserProfile: UserProfile?,
  onEvent: (UiEvent) -> Unit
) {
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
private fun CustomAuthenticatorStatusSection(isCustomAuthenticatorRegisteredForUser: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.custom_authenticator_status_title),
    status = isCustomAuthenticatorRegisteredForUser,
    tooltipContent = { Text(stringResource(R.string.custom_authenticator_requirement_tooltip)) }
  )
}

@Composable
private fun AuthenticatedProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_authenticated_user_profile)
  )
}

@Composable
private fun AuthenticationButton(isEnabled: Boolean, isLoading: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartCustomAuthentication) },
    enabled = isEnabled,
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.authenticate))
    }
  }
}

@Composable
private fun CustomAuthenticationResult(result: Result<CustomInfo?, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text(stringResource(R.string.custom_authenticator_authentication_success))
          Text(stringResource(R.string.custom_info, it.toString()))
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun ListenForNavigationEvents(
  navigationEvents: Flow<NavigationEvent>,
  onNavigateToPinScreen: () -> Unit,
  onNavigateToSdkInitialization: () -> Unit
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.ToPinScreen -> onNavigateToPinScreen()
        is NavigationEvent.ToSdkInitialization -> onNavigateToSdkInitialization()
      }
    }
  }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
  CustomAuthenticationContent(
    uiState = State(),
    navigationEvents = emptyFlow(),
    onEvent = {},
    onNavigateBack = {},
    onNavigateToPinScreen = {},
    onNavigateToSdkInitialization = {}
  )
}
