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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTooltip
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
    description = { FeatureDescription() },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { RegistrationResult(uiState) } },
    action = { RegistrationButton(uiState, onEvent) }
  )
}

@Composable
private fun FeatureDescription() {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    FeatureDescriptionHeader()
    FeatureDescriptionBody()
  }
}

@Composable
private fun FeatureDescriptionBody() {
  Text(
    style = MaterialTheme.typography.bodyLarge,
    text = buildAnnotatedString {
      append(stringResource(R.string.read_more) + " ")
      withLink(
        LinkAnnotation.Url(
          Constants.DOCUMENTATION_SDK_INITIALIZATION,
          TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary))
        )
      ) {
        append(stringResource(R.string.here))
      }
    })
}

@Composable
private fun FeatureDescriptionHeader() {
  Text(
    style = MaterialTheme.typography.bodyLarge,
    text = stringResource(R.string.browser_registration_description)
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
  ) {
    SdkInitializationSection(uiState)
    UserProfilesSection(uiState)
    IdentityProvidersSection(uiState, onEvent)
    ScopesSection(onEvent)
  }
}

@Composable
private fun ScopesSection(onEvent: (UiEvent) -> Unit) {
  ScopesHeader()
  ScopesList(onEvent)
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
    var selectedIdentityProvider by remember { mutableStateOf(uiState.identityProviders[0]) }
    Column {
      IdentityProvidersHeader()
      IdentityProvidersList(uiState, selectedIdentityProvider, onEvent)
      DefaultIdentityProvider(uiState.shouldUseDefaultIdentityProvider, onEvent)
    }
  }
}

@Composable
private fun IdentityProvidersList(
  uiState: State,
  selectedIdentityProvider: BrowserIdentityProvider,
  onEvent: (UiEvent) -> Unit
) {
  var selectedIdentityProvider1 = selectedIdentityProvider
  uiState.identityProviders.forEach { identityProvider ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable { selectedIdentityProvider1 = identityProvider }
        .padding(bottom = Dimensions.sPadding)
    ) {
      RadioButton(
        selected = (identityProvider == selectedIdentityProvider1),
        onClick = {
          selectedIdentityProvider1 = identityProvider
          onEvent
            .invoke(UiEvent.UpdateSelectedIdentityProvider(selectedIdentityProvider1))
        }
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
private fun UserProfilesSection(uiState: State) {
  uiState.userProfiles
    ?.onSuccess { UserProfilesSection(it.separateItemsWithComa()) }
    ?.onFailure { UserProfilesSection(stringResource(R.string.no_user_profiles)) }
}

@Composable
private fun SdkInitializationSection(uiState: State) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = uiState.isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_registration)) }
  )
}

@Composable
private fun UserProfilesSection(userProfiles: String) {
  ShowcaseStatusCard(
    title = stringResource(R.string.user_profiles),
    description = userProfiles
  )
}

@Composable
private fun RegistrationResult(uiState: State) {
  Column {
    uiState.result
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
private fun DefaultIdentityProvider(
  shouldUseDefaultIdentityProvider: Boolean,
  onEvent: (UiEvent) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(stringResource(R.string.use_default_identity_provider), modifier = Modifier.weight(1f))
    Switch(
      checked = shouldUseDefaultIdentityProvider,
      onCheckedChange = { onEvent.invoke(UiEvent.UseDefaultIdentityProvider(it)) })
    ShowcaseTooltip { Text(stringResource(R.string.default_identity_provider_tooltip_text)) }
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
