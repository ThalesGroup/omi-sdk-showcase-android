package com.onewelcome.showcaseapp.feature.userderegistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.handlers.error.OneginiError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseDropdownMenu
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userderegistration.UserDeregistrationViewModel.UiEvent

@Composable
fun UserDeregistrationScreen(
  navController: NavController,
  viewModel: UserDeregistrationViewModel = hiltViewModel()
) {
  UserDeregistrationScreenContent(
    viewModel.uiState,
    { navController.popBackStack() },
    { viewModel.onEvent(it) }
  )
}

@Composable
private fun UserDeregistrationScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit
) {
  LaunchedEffect(Unit) {
    onEvent.invoke(UiEvent.LoadInitialData)
  }
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_user_deregistration),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.user_deregistration_description),
        Constants.DOCUMENTATION_USER_DEREGISTRATION
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.result?.let { { DeregistrationResult(it) } },
    action = { DeregisterUserButton(uiState, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    RegisteredUserProfilesSection(uiState.registeredUserProfiles)
    Text(
      text = stringResource(R.string.required),
      style = MaterialTheme.typography.titleSmall
    )
    UserProfileSelectionSection(uiState.registeredUserProfiles, uiState.selectedUserProfile, onEvent)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_deregistration)) }
  )
}

@Composable
private fun RegisteredUserProfilesSection(registeredUserProfiles: Set<UserProfile>) {
  val description = registeredUserProfiles.map { it.profileId }
    .separateItemsWithComa()
    .ifEmpty { stringResource(R.string.no_user_profiles) }
  ShowcaseStatusCard(
    title = stringResource(R.string.user_profiles),
    description = description,
    status = registeredUserProfiles.isNotEmpty(),
    tooltipContent = { Text(stringResource(R.string.user_profiles_requirement_tooltip)) }
  )
}

@Composable
private fun UserProfileSelectionSection(
  registeredUserProfiles: Set<UserProfile>,
  selectedUserProfile: UserProfile?,
  onEvent: (UiEvent) -> Unit
) {
  ShowcaseDropdownMenu(
    label = {
      Text(stringResource(R.string.label_user_profile))
    },
    itemList = registeredUserProfiles.toList(),
    selectedItem = selectedUserProfile,
    valueFormatter = { it.profileId },
    tooltipContent = { Text(stringResource(R.string.documentation_choose_user_profile)) },
    onItemSelected = { item, _ -> onEvent.invoke(UiEvent.OnUserProfileSelected(item)) }
  )
}

@Composable
private fun DeregistrationResult(result: Result<Unit, Throwable>) {
  Column {
    result.onSuccess { Text(stringResource(R.string.label_user_deregistration_success)) }
      .onFailure {
        val errorText = when (it) {
          is OneginiError -> "${it.errorType.code}: ${it.message}"
          else -> "$it"
        }
        Text(errorText)
      }
  }
}

@Composable
private fun DeregisterUserButton(uiState: State, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { if (uiState.isLoading.not()) onEvent(UiEvent.DeregisterUser) },
    enabled = uiState.selectedUserProfile != null
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_deregister_user))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  UserDeregistrationScreenContent(
    State(),
    {},
    {})
}
