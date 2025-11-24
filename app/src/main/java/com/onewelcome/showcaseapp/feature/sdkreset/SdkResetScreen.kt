package com.onewelcome.showcaseapp.feature.sdkreset

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
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.sdkreset.SdkResetViewModel.State
import com.onewelcome.showcaseapp.feature.sdkreset.SdkResetViewModel.UiEvent

@Composable
fun SdkResetScreen(
  homeNavController: NavHostController,
  viewModel: SdkResetViewModel = hiltViewModel()
) {
  SdkResetScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { homeNavController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
  )
}

@Composable
private fun SdkResetScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_sdk_reset),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        description = stringResource(R.string.sdk_reset_description),
        link = Constants.DOCUMENTATION_SDK_RESET
      )
    },
    settings = { SettingSection(isSdkInitialized = uiState.isSdkInitialized) },
    result = uiState.result?.let { { SdkResetResult(it) } },
    action = { SdkResetButton(onEvent, uiState.isLoading) })
}

@Composable
private fun SdkResetButton(onEvent: (UiEvent) -> Unit, isLoading: Boolean) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { if (isLoading.not()) onEvent(UiEvent.ResetSdk) },
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_sdk_reset))
    }
  }
}

@Composable
private fun SdkResetResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess {
        Column {
          Text(stringResource(R.string.sdk_reset_success))
        }
      }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun SettingSection(isSdkInitialized: Boolean) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    SdkInitializationSection(isSdkInitialized)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_reset_tooltip)) }
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SdkResetScreenContent(
    State(),
    {},
    {}
  )
}
