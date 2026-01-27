package com.onewelcome.showcaseapp.feature.tokens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.tokens.TokensViewModel.Event
import com.onewelcome.showcaseapp.feature.tokens.TokensViewModel.State

@Composable
fun TokensScreen(
  navController: NavController,
  viewModel: TokensViewModel = hiltViewModel()
) {
  TokensScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = viewModel::onEvent,
    getFormattedIdToken = viewModel::getFormattedIdToken
  )
}

@Composable
private fun TokensScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (Event) -> Unit,
  getFormattedIdToken: (String) -> String
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_tokens),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.tokens_description),
        link = Constants.DOCUMENTATION_ID_TOKENS
      )
    },
    settings = { SettingsSection(uiState) },
    action = { GetIdTokenButton(onEvent) },
    result = uiState.idTokenResult?.let { { IdTokenResult(it, getFormattedIdToken) } }
  )
}

@Composable
private fun GetIdTokenButton(onEvent: (Event) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(Event.GetIdToken) },
  ) {
    Text(stringResource(R.string.tokens_get_id_token_button))
  }
}

@Composable
private fun IdTokenResult(
  result: Result<String, Throwable>,
  getFormattedIdToken: (String) -> String
) {
  Column {
    result
      .onSuccess {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
          Text(
            stringResource(R.string.tokens_id_token_raw), style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = it,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodySmall
          )
          Text(
            stringResource(R.string.tokens_id_token_decoded), style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = getFormattedIdToken(it),
            style = MaterialTheme.typography.bodySmall
          )
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
  TokensScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {},
    getFormattedIdToken = { it }
  )
}
