package com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTooltip
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.UiEvent

@Composable
fun BiometricAuthenticationScreen(
  homeNavController: NavHostController,
  viewModel: BiometricAuthenticationViewModel = hiltViewModel()
) {
  BiometricAuthenticationContent(
    uiState = viewModel.uiState,
    onEvent = { viewModel.onEvent(it) },
    onNavigateBack = { homeNavController.popBackStack() })
}

@Composable
private fun BiometricAuthenticationContent(uiState: State, onEvent: (UiEvent) -> Unit, onNavigateBack: () -> Unit) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_biometric_authentication),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.biometric_authentication_description),
        link = Constants.DOCUMENTATION_BIOMETRIC_AUTHENTICATION
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = null,
    action = { AuthenticationButton(uiState.isAuthenticateButtonEnabled, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserProfilesSection(uiState.userProfiles, uiState.selectedUserProfile, onEvent)
    BiometricAuthenticatorStatusSection(uiState.isBiometricAuthenticatorRegisteredForUser)
    AuthenticatedProfileSection(uiState.authenticatedUserProfile)
  }
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
private fun BiometricAuthenticatorStatusSection(isBiometricAuthenticatorRegisteredForUser: Boolean) {
  ShowcaseStatusCard(
    title = "Biometric authenticator registered for selected user",
    status = isBiometricAuthenticatorRegisteredForUser,
    tooltipContent = { Text(stringResource(R.string.biometric_authenticator_requirement_tooltip)) }
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
private fun AuthenticationButton(isEnabled: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartBiometricAuthentication) },
    enabled = isEnabled,
  ) {
    Text(stringResource(R.string.authenticate))
  }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
  BiometricAuthenticationContent(
    uiState = State(),
    onEvent = {},
    onNavigateBack = {}
  )
}
