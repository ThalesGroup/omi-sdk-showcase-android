package com.onewelcome.showcaseapp.feature.logout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.logout.LogoutViewModel.State
import com.onewelcome.showcaseapp.feature.logout.LogoutViewModel.UiEvent


@Composable
fun LogoutScreen(
  homeNavController: NavHostController,
  viewModel: LogoutViewModel = hiltViewModel()
) {
  LogoutScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { homeNavController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
  )
}

@Composable
private fun LogoutScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
) {
  SdkFeatureScreen(
    title = stringResource(R.string.logout_title),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.logout_description),
        link = Constants.DOCUMENTATION_LOGOUT
      )
    },
    settings = { SettingSection(isSdkInitialized = uiState.isSdkInitialized, authenticatedUserProfile = uiState.authenticatedUserProfile) },
    result = uiState.result?.let { { LogoutResult(it) } },
    action = { LogoutButton(onEvent, uiState.isLoading) })
}

@Composable
private fun LogoutButton(onEvent: (UiEvent) -> Unit, isLoading: Boolean) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { if (isLoading.not()) onEvent(UiEvent.LogoutUser) },
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_logout_user))
    }
  }
}

@Composable
private fun LogoutResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text(stringResource(R.string.logout_performed_successfully))
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun SettingSection(isSdkInitialized: Boolean, authenticatedUserProfile: UserProfile?) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(isSdkInitialized)
    AuthenticatedProfileSection(authenticatedUserProfile)
  }
}

@Composable
private fun AuthenticatedProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_authenticated_user_profile),
  )
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_logout_tooltip)) }
  )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  LogoutScreenContent(
    State(),
    {},
    {}
  )
}

