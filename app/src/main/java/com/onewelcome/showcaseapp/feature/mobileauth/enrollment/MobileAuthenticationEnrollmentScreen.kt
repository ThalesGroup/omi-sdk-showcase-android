package com.onewelcome.showcaseapp.feature.mobileauth.enrollment

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
import com.onegini.mobile.sdk.android.handlers.error.OneginiError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentViewModel.State
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationEnrollmentViewModel.UiEvent

@Composable
fun MobileAuthenticationEnrollmentScreen(
  navController: NavHostController,
  viewModel: MobileAuthenticationEnrollmentViewModel = hiltViewModel()
) {
  MobileAuthenticationEnrollmentScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) })
}

@Composable
private fun MobileAuthenticationEnrollmentScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_mobile_authentication_enrollment),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.mobile_authentication_enrollment_description),
        Constants.DOCUMENTATION_MOBILE_AUTHENTICATION
      )
    },
    settings = { SettingsSection(uiState) },
    result = uiState.enrollmentResult?.let { { EnrollmentResult(it) } },
    action = { EnrollmentButton(uiState, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: State) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserAuthenticatedSection(uiState.authenticatedUserProfile)
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
private fun UserAuthenticatedSection(authenticatedUserProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_user_profile),
    description = authenticatedUserProfile?.let { stringResource(R.string.user_profile_id, it.profileId) },
    status = authenticatedUserProfile != null,
    tooltipContent = { Text(stringResource(R.string.mobile_auth_enrollemnt_authenticated_user_requirement_tooltip)) }
  )
}

@Composable
private fun EnrollmentResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess { Text(stringResource(R.string.label_mobile_authentication_enrollment_success)) }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

private fun Throwable.toErrorResultString(): String {
  return when (this) {
    is OneginiError -> "${this.errorType.code}: ${this.message}"
    else -> "$this"
  }
}

@Composable
private fun EnrollmentButton(uiState: State, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { if (uiState.isLoading.not()) onEvent(UiEvent.EnrollForMobileAuthentication) },
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_mobile_authentication_enrollment))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  MobileAuthenticationEnrollmentScreenContent(State(), {}, {})
}
