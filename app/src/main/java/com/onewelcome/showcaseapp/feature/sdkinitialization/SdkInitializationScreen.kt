package com.onewelcome.showcaseapp.feature.sdkinitialization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCheckbox
import com.onewelcome.core.components.ShowcaseExpandableCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseNumberTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationViewModel.State
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationViewModel.UiEvent

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
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_sdk_initialization),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.sdk_initialization_description),
        Constants.DOCUMENTATION_SDK_INITIALIZATION
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { InitializationResult(uiState) } },
    action = { InitializeSdkButton(uiState, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
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
      title = stringResource(R.string.label_sdk_settings)
    ) { SdkSettings(uiState, onEvent) }
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
fun SdkSettings(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(
    modifier = Modifier.padding(Dimensions.mPadding)
  ) {
    ShowcaseNumberTextField(
      modifier = Modifier.fillMaxWidth(),
      value = uiState.deviceConfigCacheDurationSeconds,
      onValueChange = { onEvent(UiEvent.ChangeDeviceConfigCacheDurationValue(it)) },
      label = {
        Text(
          text = stringResource(R.string.option_set_device_config_cache_duration),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      tooltipContent = { Text(stringResource(R.string.documentation_set_device_config_cache_duration)) }
    )
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

@Composable
private fun InitializationResult(uiState: State) {
  Column {
    uiState.result
      ?.onSuccess { removedUserProfiles ->
        Text(
          buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
              appendLine(stringResource(R.string.label_initialization_success))
            }
            append(stringResource(R.string.label_removed_profiles))
            append(": ")
            if (removedUserProfiles.isEmpty()) {
              append(stringResource(R.string.zero))
            } else {
              appendLine()
              removedUserProfiles.forEach { userProfile ->
                append("\u2022 ")
                appendLine(userProfile.profileId)
              }
            }
          })
      }
      ?.onFailure {
        Text("${it.errorType.code}: ${it.message}")
      }
  }
}

@Composable
private fun InitializeSdkButton(uiState: State, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { if (uiState.isLoading.not()) onEvent(UiEvent.InitializeOneginiSdk) },
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_initialize_sdk))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SdkInitializationScreenContent(
    uiState = State(isLoading = true, result = Ok(setOf(UserProfile("123456"), UserProfile("QWERTY")))),
    onNavigateBack = {},
    onEvent = {})
}
