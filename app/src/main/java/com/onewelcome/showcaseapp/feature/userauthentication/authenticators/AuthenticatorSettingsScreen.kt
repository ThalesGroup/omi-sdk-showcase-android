package com.onewelcome.showcaseapp.feature.userauthentication.authenticators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.ShowcaseCardWithProgressIndicator
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseSwitch
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorSettingsViewModel.AuthenticatorOperationResult
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorSettingsViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorSettingsViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorSettingsViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow

@Composable
fun AuthenticatorSettingsScreen(
  homeNavController: NavHostController,
  pinNavController: NavHostController,
  viewModel: AuthenticatorSettingsViewModel = hiltViewModel()
) {
  AuthenticatorSettingsContent(
    uiState = viewModel.uiState,
    onEvent = { viewModel.onEvent(it) },
    onNavigateBack = { homeNavController.popBackStack() }
  )
  ListenForPinNavigationEvent(
    navigationEvents = viewModel.navigationEvents,
    onNavigateToPinAuthenticationInputScreen = { pinNavController.navigate(Screens.PinAuthenticationInput.route) }
  )
}

@Composable
private fun AuthenticatorSettingsContent(
  uiState: State,
  onEvent: (UiEvent) -> Unit,
  onNavigateBack: () -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_authenticator_settings),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.authenticator_settings_description),
        Constants.DOCUMENTATION_AUTHENTICATOR_SETTINGS
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { ToggleAuthenticatorResult(it) } },
    action = { }
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    AuthenticatedUserSection(uiState.authenticatedUserProfile)
    if (uiState.availableAuthenticators.isNotEmpty()) {
      AuthenticatorsSection(uiState.availableAuthenticators, uiState.isLoading, onEvent)
    }
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.authenticator_settings_sdk_initialized_requirement_tooltip)) }
  )
}

@Composable
private fun AuthenticatedUserSection(authenticatedUserProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = authenticatedUserProfile?.let { stringResource(R.string.user_profile_id, it.profileId) },
    status = authenticatedUserProfile != null,
    tooltipContent = { Text(stringResource(R.string.authenticator_settings_authenticated_user_requirement_tooltip)) }
  )
}

@Composable
private fun AuthenticatorsSection(availableAuthenticators: Set<OneginiAuthenticator>, isLoading: Boolean, onEvent: (UiEvent) -> Unit) {
  ShowcaseCardWithProgressIndicator(isLoading) {
    Column {
      AuthenticatorsHeader()
      AuthenticatorsList(availableAuthenticators, onEvent)
    }
  }
}

@Composable
private fun AuthenticatorsHeader() {
  Text(
    text = stringResource(R.string.authenticator_settings_authenticators_header),
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier.padding(bottom = Dimensions.mPadding)
  )
}

@Composable
private fun AuthenticatorsList(availableAuthenticators: Set<OneginiAuthenticator>, onEvent: (UiEvent) -> Unit) {
  Column {
    availableAuthenticators.forEach { authenticator ->
      ShowcaseSwitch(
        shouldBeChecked = authenticator.isRegistered,
        onCheck = { onEvent(UiEvent.ToggleAuthenticator(authenticator)) },
        label = { Text(authenticator.name, style = MaterialTheme.typography.titleMedium) }
      )
    }
  }
}

@Composable
private fun ToggleAuthenticatorResult(result: AuthenticatorOperationResult) {
  Column {
    when (result) {
      is AuthenticatorOperationResult.DeregisterSuccess ->
        Text(stringResource(R.string.authenticator_settings_deregistration_success))

      is AuthenticatorOperationResult.RegisterSuccess ->
        Text(stringResource(R.string.authenticator_settings_registration_success, result.customInfo.toString()))

      is AuthenticatorOperationResult.Error ->
        Text(result.throwable.toErrorResultString())
    }
  }
}

@Composable
private fun ListenForPinNavigationEvent(
  navigationEvents: Flow<NavigationEvent>,
  onNavigateToPinAuthenticationInputScreen: () -> Unit
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.ToPinAuthenticationScreen -> onNavigateToPinAuthenticationInputScreen.invoke()
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  AuthenticatorSettingsContent(
    uiState = State(
      isSdkInitialized = true,
      authenticatedUserProfile = UserProfile("QWERTY"),
      availableAuthenticators = setOf(
        object : OneginiAuthenticator {
          override val id: String = "pin"
          override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.PIN
          override val name: String = "PIN"
          override val isRegistered: Boolean = true
          override val isPreferred: Boolean = true
          override val userProfile: UserProfile = UserProfile.default
        },
        object : OneginiAuthenticator {
          override val id: String = "biometric"
          override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.BIOMETRIC
          override val name: String = "Biometric"
          override val isRegistered: Boolean = false
          override val isPreferred: Boolean = false
          override val userProfile: UserProfile = UserProfile.default

        }),
      isLoading = true
    ),
    onEvent = {},
    onNavigateBack = {})
}
