package com.onewelcome.showcaseapp.feature.singlesignon

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


@Composable
fun SingleSignOnScreen(
  navController: NavController,
  viewModel: SingleSignOnViewModel = hiltViewModel()
) {
  SingleSignOnScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = viewModel::onEvent,
    navigationEvent = viewModel.navigationEvents,
  )
}

@Composable
private fun SingleSignOnScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (Event) -> Unit,
  navigationEvent: Flow<SingleSignOnViewModel.NavigationEvent>
) {
  navigationEventListener(navigationEvent)
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
private fun navigationEventListener(navigationEvent: Flow<SingleSignOnViewModel.NavigationEvent>) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    navigationEvent.collect {
      when (it) {
        is SingleSignOnViewModel.NavigationEvent.OpenUrl -> {
          Intent(Intent.ACTION_VIEW, it.uri)
            .apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            }
            .let { uri -> context.startActivity(uri) }
        }
      }
    }
  }
}

@Composable
private fun SsoButton(onEvent: (Event) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(Event.PerformSingleSignOn(Constants.SSO_URL.toUri())) },
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
            stringResource(R.string.single_sign_on_redirect_url), style = MaterialTheme.typography.titleMedium
          )
          Text(it.redirectUrl.toString())
          Text(
            stringResource(R.string.token), style = MaterialTheme.typography.titleMedium
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
    onEvent = {},
    navigationEvent = flowOf()
  )
}
