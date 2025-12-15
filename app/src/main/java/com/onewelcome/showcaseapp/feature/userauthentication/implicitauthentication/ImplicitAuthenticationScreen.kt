package com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication.ImplicitAuthenticationViewModel.UiEvent

@Composable
fun ImplicitAuthenticationScreen(
  navController: NavController,
  viewModel: ImplicitAuthenticationViewModel = hiltViewModel()
) {
  ImplicitAuthenticationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) }
  )
}

@Composable
private fun ImplicitAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  uiState: State,
  onEvent: (UiEvent) -> Unit,
) {
  SdkFeatureScreen(
    title = stringResource(R.string.implicit_authentication),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.implicit_authentication_description),
        link = Constants.DOCUMENTATION_IMPLICIT_AUTHENTICATION
      )
    },
    settings = { SettingSection(uiState, onEvent) },
    result = uiState.result?.let { { ImplicitAuthenticationResult(it) } },
    action = { AuthenticationButton(uiState.isAuthenticateButtonEnabled, onEvent) }
  )
}

@Composable
private fun SettingSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfiles, uiState.selectedUserProfile, onEvent)
    ScopesSection(uiState.isSdkInitialized, onEvent)
    AuthenticatedProfileSection(uiState.implicitlyAuthenticatedUserProfile)
  }
}

@Composable
private fun AuthenticatedProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.implicit_authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_implicitly_authenticated_user_profile)
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
private fun AuthenticationButton(isEnabled: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartImplicitAuthentication) },
    enabled = isEnabled,
  ) {
    Text(stringResource(R.string.authenticate))
  }
}

@Composable
private fun ImplicitAuthenticationResult(result: Result<UserProfile, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text(stringResource(R.string.implicit_authentication_successful))
          Text(stringResource(R.string.user_profile, it.profileId))
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
  ImplicitAuthenticationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {}
  )
}
