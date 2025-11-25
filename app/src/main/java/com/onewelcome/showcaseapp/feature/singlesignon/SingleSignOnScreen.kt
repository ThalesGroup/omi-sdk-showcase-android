package com.onewelcome.showcaseapp.feature.singlesignon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiAppToWebSingleSignOn
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.singlesignon.SingleSignOnViewModel.Event
import com.onewelcome.showcaseapp.feature.singlesignon.SingleSignOnViewModel.State

@Composable
fun SingleSignOnScreen(
  navController: NavController,
  viewModel: SingleSignOnViewModel = hiltViewModel()
) {
  viewModel.updateData()
  SingleSignOnScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = viewModel::onEvent,
  )
}

@Composable
private fun SingleSignOnScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (Event) -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_single_sign_on),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.single_sign_on_description),
        link = Constants.DOCUMENTATION_SINGLE_SIGN_ON
      )
    },
    settings = { SettingsSection(uiState) },
    action = { SsoButton(onEvent) },
    result = uiState.result?.let { { SsoResult(it) } }
  )
}

@Composable
private fun SsoButton(onEvent: (Event) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(Event.OpenUrl(Constants.SSO_URL.toUri())) },
  ) {
    Text(stringResource(R.string.single_sign_on_button))
  }
}

@Composable
private fun SsoResult(result: Result<OneginiAppToWebSingleSignOn, Throwable>) {
  Column {
    result
      .onSuccess {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
          Text(
            "Redirect url", style = MaterialTheme.typography.titleMedium
          )
          Text(it.redirectUrl.toString())
          Text(
            "Token", style = MaterialTheme.typography.titleMedium
          )
          Text(it.token)

        }
      }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun SettingsSection(uiState: State) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(uiState.isSdkInitialized)
    AuthenticatedUserProfileSection(uiState.userProfile)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized
  )
}

@Composable
private fun AuthenticatedUserProfileSection(userProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = userProfile?.profileId ?: stringResource(R.string.no_authenticated_user_profile),
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SingleSignOnScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {}
  )
}
