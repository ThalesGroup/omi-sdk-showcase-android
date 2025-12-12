package com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication.BiometricAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun BiometricAuthenticationScreen(
  homeNavController: NavHostController,
  pinNavController: NavController,
  viewModel: BiometricAuthenticationViewModel = hiltViewModel()
) {
  BiometricAuthenticationContent(
    uiState = viewModel.uiState,
    navigationEvents = viewModel.navigationEvents,
    onEvent = { viewModel.onEvent(it) },
    onNavigateBack = { homeNavController.popBackStack() },
    onNavigateToPinScreen = { pinNavController.navigate(Screens.PinAuthenticationInput.route) })
}

@Composable
private fun BiometricAuthenticationContent(
  uiState: State,
  navigationEvents: Flow<NavigationEvent>,
  onEvent: (UiEvent) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateToPinScreen: () -> Unit
) {
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
    result = uiState.result?.let { { BiometricAuthenticationResult(it) } },
    action = { AuthenticationButton(uiState.isAuthenticateButtonEnabled, uiState.isLoading, onEvent) }
  )
  ListenForNavigationEvents(navigationEvents, onEvent, onNavigateToPinScreen)
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
private fun AuthenticationButton(isEnabled: Boolean, isLoading: Boolean, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartBiometricAuthentication) },
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
private fun BiometricAuthenticationResult(result: Result<Pair<UserProfile, CustomInfo?>, Throwable>) {
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

@Composable
private fun ListenForNavigationEvents(
  navigationEvents: Flow<NavigationEvent>,
  onEvent: (UiEvent) -> Unit,
  onNavigateToPinScreen: () -> Unit
) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.ShowBiometricPrompt -> showBiometricPrompt(context, event.cryptoObject, onEvent)
        is NavigationEvent.ToPinScreen -> onNavigateToPinScreen()
      }
    }
  }
}

private fun showBiometricPrompt(context: Context, cryptoObject: BiometricPrompt.CryptoObject, onEvent: (UiEvent) -> Unit) {
  val executor = ContextCompat.getMainExecutor(context)
  if (context is FragmentActivity) {
    val biometricPrompt = BiometricPrompt(context, executor, getBiometricPromptAuthenticationCallback(onEvent))
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(context.getString(R.string.biometric_prompt_title))
      .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
      .setNegativeButtonText(context.getString(R.string.biometric_prompt_negative_button))
      .build()
    biometricPrompt.authenticate(promptInfo, cryptoObject)
  }
}

private fun getBiometricPromptAuthenticationCallback(onEvent: (UiEvent) -> Unit) = object : BiometricPrompt.AuthenticationCallback() {
  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)
    onEvent(UiEvent.BiometricAuthenticationSuccess)
  }

  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)
    onEvent(UiEvent.BiometricAuthenticationError(errorCode))
  }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
  BiometricAuthenticationContent(
    uiState = State(),
    navigationEvents = emptyFlow(),
    onEvent = {},
    onNavigateBack = {},
    onNavigateToPinScreen = {}
  )
}
