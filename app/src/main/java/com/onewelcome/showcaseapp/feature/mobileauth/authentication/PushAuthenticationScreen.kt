package com.onewelcome.showcaseapp.feature.mobileauth.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.mobileauth.authentication.PushAuthenticationViewModel.State
import com.onewelcome.showcaseapp.feature.mobileauth.authentication.PushAuthenticationViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.mobileauth.authentication.PushAuthenticationViewModel.UiEvent.AuthenticateWithPush

@Composable
fun PushAuthenticationScreen(navController: NavHostController, viewModel: PushAuthenticationViewModel = hiltViewModel()) {
  PushAuthenticationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) }
  )
}

@Composable
private fun PushAuthenticationScreenContent(uiState: State, onNavigateBack: () -> Unit, onEvent: (UiEvent) -> Unit) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_push_authentication),
    onNavigateBack = onNavigateBack,
    description = { ShowcaseFeatureDescription("", "") },
    settings = { SettingsSection(uiState) },
    result = uiState.result?.let { { PushAuthenticationResult(it) } },
    action = { PushAuthenticationButton(onEvent) },
  )
}

@Composable
private fun PushAuthenticationButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(AuthenticateWithPush) },
  ) {
    Text(stringResource(R.string.authenticate))
  }
}

@Composable
private fun PushAuthenticationResult(result: Result<Unit, Throwable>) {
  Column { }
}

@Composable
private fun SettingsSection(uiState: State) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserAuthenticatedSection(uiState.authenticatedUserProfile)
    UserEnrolledForMobileAuthSection(uiState.isUserEnrolledForMobileAuth)
    UserEnrolledForMobileAuthWithPushSection(uiState.isUserEnrolledForMobileAuthWithPush)
    PostNotificationPermissionSection(uiState.isPostNotificationPermissionGranted)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_authentication)) }
  )
}

@Composable
private fun UserAuthenticatedSection(authenticatedUserProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = authenticatedUserProfile?.let { stringResource(R.string.user_profile_id, it.profileId) },
    status = authenticatedUserProfile != null,
    tooltipContent = { Text(stringResource(R.string.mobile_auth_push_authentication_authenticated_user_requirement_tooltip)) }
  )
}

@Composable
private fun UserEnrolledForMobileAuthSection(isUserEnrolledForMobileAuth: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_user_enrolled_for_mobile_authentication),
    status = isUserEnrolledForMobileAuth,
    tooltipContent = { Text(stringResource(R.string.user_needs_to_be_enrolled_for_mobile_authentication)) }
  )
}

@Composable
private fun UserEnrolledForMobileAuthWithPushSection(isUserEnrolledForMobileAuthWithPush: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_user_enrolled_for_mobile_authentication_with_push),
    status = isUserEnrolledForMobileAuthWithPush,
    tooltipContent = { Text(stringResource(R.string.user_enrolled_for_mobile_authentication_with_push_tooltip)) }
  )
}

@Composable
private fun PostNotificationPermissionSection(
  isPostNotificationPermissionGranted: Boolean,
) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_post_notifications_permission),
    status = isPostNotificationPermissionGranted,
    tooltipContent = { Text(stringResource(R.string.post_notifications_permission_tooltip)) }
  )
}
