package com.onewelcome.showcaseapp.feature.userregistration.onestepregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.onewelcome.core.components.ShowcaseSwitch
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun OneStepRegistrationScreen(
  navController: NavController,
  pinNavController: NavController,
  viewModel: OneStepRegistrationViewModel = hiltViewModel()
) {
  OneStepRegistrationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    onNavigateToPinScreen = { pinNavController.navigate(Screens.CreatePinInput.route) },
    navigationEvents = viewModel.navigationEvents
  )
}

@Composable
private fun OneStepRegistrationScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  onNavigateToPinScreen: () -> Unit,
  navigationEvents: Flow<NavigationEvent>,
) {
  ListenForPinNavigationEvent(navigationEvents, onNavigateToPinScreen)
  SdkFeatureScreen(
    title = stringResource(R.string.one_step_registration),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.one_step_registration_description),
        link = "https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-using/android-sdk-register-user/index.html#one-step-registration"
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { RegistrationResult(uiState.result) } },
    action = {
      RegistrationButton(onEvent)
      CancellationButton(uiState.isRegistrationCancellationEnabled, onEvent)
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
        is NavigationEvent.ToPinScreen -> {
          onNavigateToPinScreen.invoke()
        }
      }
    }
  }
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfileIds)
    ScopesSection(uiState.isSdkInitialized, onEvent)
    StatelessRegistrationSection(uiState.isStatelessRegistration, onEvent)
  }
}

@Composable
private fun UserProfilesSection(userProfiles: List<String>) {
  val text = getUserProfilesText(userProfiles)
  UserProfilesCard(text)
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
private fun UserProfilesCard(userProfiles: String) {
  ShowcaseStatusCard(
    title = stringResource(R.string.user_profiles),
    description = userProfiles
  )
}

@Composable
private fun RegistrationResult(userProfilesResult: Result<Pair<UserProfile, CustomInfo?>, Throwable>?) {
  Column {
    userProfilesResult
      ?.onSuccess {
        Column {
          Text(stringResource(R.string.registration_successful))
          Text(stringResource(R.string.user_profile, it.first.profileId))
          Text(stringResource(R.string.custom_info, it.second.toString()))
        }
      }
      ?.onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun RegistrationButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartOneStepRegistration) }
  ) {
    Text(stringResource(R.string.register))
  }
}

@Composable
private fun CancellationButton(isRegistrationCancellationEnabled: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.CancelRegistration) },
    enabled = isRegistrationCancellationEnabled
  ) {
    Text(stringResource(R.string.cancel_registration))
  }
}

@Composable
private fun ScopesSection(isSdkInitialized: Boolean, onEvent: (UiEvent) -> Unit) {
  if (isSdkInitialized) {
    ShowcaseCard {
      Column {
        ScopesHeader()
        ScopesList(onEvent)
      }
    }
  }
}

@Composable
private fun ScopesHeader() {
  Text(
    text = stringResource(R.string.registration_scopes),
    style = MaterialTheme.typography.titleMedium,
  )
}

@Composable
private fun ScopesList(onEvent: (UiEvent) -> Unit) {
  val scopes = Constants.DEFAULT_SCOPES
  var selectedScopes by remember { mutableStateOf(Constants.DEFAULT_SCOPES) }
  Column {
    scopes.forEach { scope ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(scope, modifier = Modifier.weight(1f))
        Checkbox(
          checked = selectedScopes.contains(scope),
          onCheckedChange = { isChecked ->
            selectedScopes = if (isChecked) {
              selectedScopes + scope
            } else {
              selectedScopes - scope
            }
            onEvent.invoke(UiEvent.UpdateSelectedScopes(selectedScopes))
          }
        )
      }
    }
  }
}

@Composable
private fun StatelessRegistrationSection(isStatelessRegistration: Boolean, onEvent: (UiEvent) -> Unit) {
  ShowcaseCard {
    Column {
      ShowcaseSwitch(
        shouldBeChecked = isStatelessRegistration,
        onCheck = { onEvent(UiEvent.SetStatelessRegistration(it)) },
        label = {
          Text(
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(R.string.stateless_registration_label)
          )
        },
        tooltipContent = {
          Text(stringResource(R.string.stateless_registration_tooltip))
        }
      )
    }
  }
}

@Composable
private fun getUserProfilesText(userProfiles: List<String>): String {
  return if (userProfiles.isNotEmpty()) {
    userProfiles.separateItemsWithComa()
  } else {
    stringResource(R.string.no_user_profiles)
  }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  OneStepRegistrationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {},
    onNavigateToPinScreen = {},
    navigationEvents = emptyFlow(),
  )
}
