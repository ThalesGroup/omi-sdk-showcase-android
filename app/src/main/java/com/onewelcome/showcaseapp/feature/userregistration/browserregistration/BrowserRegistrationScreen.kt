package com.onewelcome.showcaseapp.feature.userregistration.browserregistration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseSwitch
import com.onewelcome.core.omisdk.entity.BrowserIdentityProvider
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.R.string.identity_providers
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent

@Composable
fun BrowserRegistrationScreen(
  navController: NavController,
  viewModel: BrowserRegistrationViewModel = hiltViewModel()
) {
  BrowserRegistrationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) }
  )
}

@Composable
private fun BrowserRegistrationScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.browser_registration),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.browser_registration_description),
        link = Constants.DOCUMENTATION_USER_REGISTRATION
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { RegistrationResult(uiState.userProfiles) } },
    action = { RegistrationButton(uiState, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfiles)
    IdentityProvidersSection(uiState, onEvent)
    ScopesSection(uiState.isSdkInitialized, onEvent)
  }
}

@Composable
private fun ScopesSection(isSdkInitialized: Boolean, onEvent: (UiEvent) -> Unit) {
  if (isSdkInitialized) {
    ScopesHeader()
    ScopesList(onEvent)
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
private fun IdentityProvidersSection(
  uiState: State,
  onEvent: (UiEvent) -> Unit
) {
  if (uiState.identityProviders.isNotEmpty()) {
    Column {
      IdentityProvidersHeader()
      IdentityProvidersList(uiState, onEvent)
      ShowcaseSwitch(
        shouldBeChecked = uiState.shouldUseDefaultIdentityProvider,
        onCheck = { onEvent.invoke(UiEvent.UseDefaultIdentityProvider(it)) },
        text = stringResource(R.string.use_default_identity_provider)
      )
    }
  }
}

@Composable
private fun IdentityProvidersList(
  uiState: State,
  onEvent: (UiEvent) -> Unit
) {
  var selectedIdentityProvider = uiState.selectedIdentityProvider ?: uiState.identityProviders[0]
  uiState.identityProviders.forEach { identityProvider ->
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
      Text("Name: ${identityProvider.name}\nID: ${identityProvider.id}")
    }
  }
}

@Composable
private fun IdentityProvidersHeader() {
  Text(
    text = stringResource(identity_providers),
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier.padding(bottom = Dimensions.mPadding)
  )
}

@Composable
private fun UserProfilesSection(userProfiles: Result<List<String>, Throwable>?) {
  val text = getUserProfilesText(userProfiles)
  userProfiles
    ?.onSuccess { UserProfilesCard(text) }
    ?.onFailure { UserProfilesCard(text) }
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
private fun RegistrationResult(userProfilesResult: Result<List<String>, Throwable>?) {
  Column {
    userProfilesResult
      ?.onSuccess { Text(stringResource(R.string.registration_successful)) }
      ?.onFailure { Text("$it") }
  }
}

@Composable
private fun RegistrationButton(uiState: State, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = {
      if (uiState.isLoading) {
        onEvent(UiEvent.CancelRegistration)
      } else {
        onEvent(UiEvent.StartBrowserRegistration)
      }
    }
  ) {
    if (uiState.isLoading) {
      Text(stringResource(R.string.cancel_registration))
    } else {
      Text(stringResource(R.string.register))
    }
  }
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
private fun getUserProfilesText(userProfiles: Result<List<String>, Throwable>?): String {
  return if (userProfiles?.isOk == true && userProfiles.value.isNotEmpty()) {
    userProfiles.value.separateItemsWithComa()
  } else {
    stringResource(R.string.no_user_profiles)
  }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  BrowserRegistrationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {}
  )
  val browserIdentityProviders = listOf(
    BrowserIdentityProvider("Browser identity provider name", "browser_identity_provider_id"),
    BrowserIdentityProvider("Another browser identity provider name with two lines", "another_browser_identity_provider_id"),
  )
  BrowserRegistrationScreenContent(
    uiState = State(identityProviders = browserIdentityProviders),
    onNavigateBack = {},
    onEvent = {}
  )
}
