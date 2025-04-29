package com.onewelcome.showcaseapp.feature.userregistration.browserregistration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
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
    Text(
      style = MaterialTheme.typography.bodyLarge,
      text = stringResource(R.string.browser_registration_description)
    )
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
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column {
    ShowcaseStatusCard(
      title = stringResource(R.string.status_sdk_initialized),
      status = uiState.isSdkInitialized,
      tooltipContent = { Text("SDK needs to be initialized to perform registration") }
    )
    IdentityProviders()
  }
}

@Composable
private fun RegistrationResult(uiState: State) {
  Column {
    uiState.result
      ?.onSuccess {
        Text("Registration is a magnificent success")
      }
      ?.onFailure {
        Text("${it.errorType.code}: ${it.message}")
      }
  }
}

@Composable
private fun RegistrationButton(uiState: State, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.StartBrowserRegistration) }
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.register))
    }
  }
}

@Composable
private fun IdentityProviders() {
  val options = listOf("Option A", "Option B", "Option C")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = Modifier.padding(top = Dimensions.mPadding)) {
    Text("Identity providers", style = MaterialTheme.typography.titleMedium)
    options.forEach { text ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { selectedOption = text }
          .padding(8.dp)
      ) {
        RadioButton(
          selected = (text == selectedOption),
          onClick = { selectedOption = text }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
      }
    }
  }
}