package com.onewelcome.showcaseapp.ui.screens.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCheckbox
import com.onewelcome.core.components.ShowcaseExpandableCard
import com.onewelcome.core.components.ShowcaseNumberTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel.State
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel.UiEvent

@Composable
fun SdkInitializationScreen(
  navController: NavController,
  viewModel: SdkInitializationViewModel = hiltViewModel()
) {
  SdkInitializationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) })
}

@Composable
private fun SdkInitializationScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit
) {
  Scaffold(
    topBar = { TopBar(onNavigateBack) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding)
    ) {
      SettingsSection(
        modifier =
          Modifier
            .weight(1f)
            .padding(bottom = Dimensions.sPadding),
        uiState = uiState,
        onEvent = onEvent
      )
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEvent(UiEvent.InitializeOneginiSdk) }
      ) {
        Text(stringResource(R.string.button_initialize_sdk))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(stringResource(R.string.section_title_sdk_initialization)) },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.content_description_navigate_back)
        )
      }
    })
}

@Composable
private fun SettingsSection(modifier: Modifier, uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    Text(
      text = stringResource(R.string.required),
      style = MaterialTheme.typography.titleSmall
    )
    RequiredSettings()
    Text(
      text = stringResource(R.string.optional),
      style = MaterialTheme.typography.titleSmall
    )
    OptionalSettings(uiState, onEvent)
  }
}

@Composable
private fun RequiredSettings() {
  ShowcaseExpandableCard(title = stringResource(R.string.label_title_handlers)) {} //TODO To be done in scope of EXAMPLEAND-153
}

@Composable
private fun OptionalSettings(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    ShowcaseExpandableCard(
      title = stringResource(R.string.label_http_settings)
    ) { HttpSettings(uiState, onEvent) }
    ShowcaseExpandableCard(
      title = stringResource(R.string.label_custom_authenticators)
    ) { } //TODO To be done in scope of EXAMPLEAND-155
    ShowcaseExpandableCard(
      title = stringResource(R.string.label_custom_identity_providers)
    ) { } //TODO To be done in scope of EXAMPLEAND-156  }
  }
}

@Composable
private fun HttpSettings(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    modifier = Modifier.padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    ShowcaseCheckbox(
      text = stringResource(R.string.option_should_store_cookies),
      checked = uiState.shouldStoreCookies,
      tooltipContent = { Text(stringResource(R.string.documentation_should_store_cookies)) }
    ) { onEvent(UiEvent.ChangeShouldStoreCookiesValue(it)) }
    ShowcaseNumberTextField(
      modifier = Modifier.fillMaxWidth(),
      value = uiState.httpConnectTimeout,
      onValueChange = { onEvent(UiEvent.ChangeHttpConnectTimeoutValue(it)) },
      label = { Text(stringResource(R.string.option_set_http_connect_timeout)) },
      tooltipContent = { Text(stringResource(R.string.documentation_set_http_connect_timeout)) }
    )
    ShowcaseNumberTextField(
      modifier = Modifier.fillMaxWidth(),
      value = uiState.httpReadTimeout,
      onValueChange = { onEvent(UiEvent.ChangeHttpReadTimeoutValue(it)) },
      label = { Text(stringResource(R.string.option_set_http_read_timeout)) },
      tooltipContent = { Text(stringResource(R.string.documentation_set_http_read_timeout)) }
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SdkInitializationScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {})
}
