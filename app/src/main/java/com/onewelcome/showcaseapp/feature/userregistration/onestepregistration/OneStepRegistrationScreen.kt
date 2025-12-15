package com.onewelcome.showcaseapp.feature.userregistration.onestepregistration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseSwitch
import com.onewelcome.core.components.ShowcaseTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userregistration.onestepregistration.OneStepRegistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import com.onewelcome.showcaseapp.navigation.getResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun OneStepRegistrationScreen(
  navController: NavController,
  pinNavController: NavController,
  rootNavController: NavController,
  viewModel: OneStepRegistrationViewModel = hiltViewModel()
) {
  ListenForQrCodeNavigationEvent(viewModel.navigationEvents, navController)

  OneStepRegistrationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    onNavigateToPinScreen = { pinNavController.navigate(Screens.CreatePinInput.route) },
    navigationEvents = viewModel.navigationEvents
  )
}

@Composable
private fun ListenForQrCodeNavigationEvent(
  navigationEvents: Flow<NavigationEvent>,
  navController: NavController
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.ToOneStepRegistrationOtpScreen -> {
          navController.navigate(Screens.OneStepRegistrationOtpScreen.route)
        }
        else -> {}
      }
    }
  }
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
        else -> {}
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
    IdentityProvidersSection(uiState, onEvent)
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
private fun IdentityProvidersSection(
  uiState: State,
  onEvent: (UiEvent) -> Unit
) {
  if (uiState.identityProviders.isNotEmpty()) {
    ShowcaseCard {
      Column {
        IdentityProvidersHeader()
        IdentityProvidersList(uiState.selectedIdentityProvider, uiState.identityProviders, onEvent)
        ShowcaseSwitch(
          shouldBeChecked = uiState.shouldUseDefaultIdentityProvider,
          onCheck = { onEvent.invoke(UiEvent.UseDefaultIdentityProvider(it)) },
          label = { Text(stringResource(R.string.use_default_identity_provider)) },
          tooltipContent = { Text(stringResource(R.string.default_identity_provider_tooltip_text)) }
        )
      }
    }
  }
}

@Composable
private fun IdentityProvidersList(
  chosenIdentityProvider: OneginiIdentityProvider?,
  identityProviders: Set<OneginiIdentityProvider>,
  onEvent: (UiEvent) -> Unit
) {
  val selectedIdentityProvider = chosenIdentityProvider ?: identityProviders.first()
  identityProviders.forEach { identityProvider ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onEvent.invoke(UiEvent.UpdateSelectedIdentityProvider(identityProvider)) }
        .padding(bottom = Dimensions.sPadding)
    ) {
      RadioButton(
        selected = (identityProvider == selectedIdentityProvider),
        onClick = { onEvent.invoke(UiEvent.UpdateSelectedIdentityProvider(identityProvider)) }
      )
      Text(stringResource(R.string.idp_item_label, identityProvider.name, identityProvider.id))
    }
  }
}

@Composable
private fun IdentityProvidersHeader() {
  Text(
    text = stringResource(R.string.identity_providers),
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier.padding(bottom = Dimensions.mPadding)
  )
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
private fun Preview() {
  OneStepRegistrationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {},
    onNavigateToPinScreen = {},
    navigationEvents = emptyFlow(),
  )
}
