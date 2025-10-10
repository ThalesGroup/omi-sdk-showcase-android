package com.onewelcome.showcaseapp.feature.mobileauth.enrollment

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.handlers.error.OneginiError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseSwitch
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.State
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.openAppSettings

@Composable
fun MobileAuthenticationWithPushEnrollmentScreen(
  navController: NavHostController,
  viewModel: MobileAuthenticationWithPushEnrollmentViewModel = hiltViewModel()
) {
  MobileAuthenticationWithPushEnrollmentScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) }
  )
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.onEvent(UiEvent.UpdatePostNotificationsPermissionState)
  }
}

@Composable
private fun MobileAuthenticationWithPushEnrollmentScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit
) {
  SdkFeatureScreen(
    title = stringResource(R.string.section_title_mobile_authentication_push_enrollment),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.mobile_authentication_push_enrollment_description),
        Constants.DOCUMENTATION_MOBILE_AUTHENTICATION_WITH_PUSH
      )
    },
    settings = { SettingsSection(uiState, onEvent) },
    result = uiState.enrollmentResult?.let { { EnrollmentResult(it) } },
    action = { EnrollmentButton(uiState, onEvent) }
  )
  if (uiState.requestPostNotificationsPermission) {
    RequestPostNotificationsPermission(onEvent)
  }
  if (uiState.showSettingsDialog) {
    ShowPermissionSettingsAlertDialog(onEvent)
  }
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    UserAuthenticatedSection(uiState.authenticatedUserProfile)
    UserEnrolledForMobileAuthSection(uiState.isUserEnrolledForMobileAuth)
    UserEnrolledForMobileAuthWithPushSection(uiState.isUserEnrolledForMobileAuthWithPush)
    PostNotificationPermissionSection(uiState.isPostNotificationPermissionGranted, onEvent)
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_enrollment)) }
  )
}

@Composable
private fun UserAuthenticatedSection(authenticatedUserProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = authenticatedUserProfile?.let { stringResource(R.string.user_profile_id, it.profileId) },
    status = authenticatedUserProfile != null,
    tooltipContent = { Text(stringResource(R.string.mobile_auth_push_enrollment_authenticated_user_requirement_tooltip)) }
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
private fun PostNotificationPermissionSection(isPostNotificationPermissionGranted: Boolean, onEvent: (UiEvent) -> Unit) {
  ShowcaseCard {
    ShowcaseSwitch(
      shouldBeChecked = isPostNotificationPermissionGranted,
      onCheck = { onEvent(UiEvent.PostNotificationsPermissionClicked(it)) },
      label = {
        Text(
          style = MaterialTheme.typography.titleMedium,
          text = stringResource(R.string.status_post_notifications_permission)
        )
      },
      tooltipContent = { Text(stringResource(R.string.post_notifications_permission_tooltip)) })
  }
}

@Composable
private fun EnrollmentResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess { Text(stringResource(R.string.label_mobile_authentication_push_enrollment_success)) }
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
    onClick = { if (uiState.isLoading.not()) onEvent(UiEvent.EnrollForMobileAuthenticationWithPush) },
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.button_mobile_authentication_push_enrollment))
    }
  }
}

@Composable
private fun ShowPermissionSettingsAlertDialog(onEvent: (UiEvent) -> Unit) {
  val activity = LocalActivity.current
  AlertDialog(
    onDismissRequest = { onEvent(UiEvent.DismissSettingsDialog) },
    title = { Text(stringResource(R.string.post_notifications_permission_dialog_title)) },
    text = { Text(stringResource(R.string.post_notifications_permission_dialog_description)) },
    confirmButton = {
      TextButton(
        onClick = {
          onEvent(UiEvent.DismissSettingsDialog)
          activity?.openAppSettings()
        }) {
        Text(stringResource(R.string.navigate_to_settings))
      }
    },
    dismissButton = {
      TextButton(onClick = { onEvent(UiEvent.DismissSettingsDialog) }) {
        Text(stringResource(R.string.cancel))
      }
    }

  )
}

@Composable
private fun RequestPostNotificationsPermission(onEvent: (UiEvent) -> Unit) {
  val activity = LocalActivity.current
  val permissionResultLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { handlePostNotificationPermissionResult(it, activity, onEvent) }
  )
  SideEffect {
    permissionResultLauncher.launchForPostNotificationPermission()
  }
}

private fun ManagedActivityResultLauncher<String, Boolean>.launchForPostNotificationPermission() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    launch(Manifest.permission.POST_NOTIFICATIONS)
  }
}

private fun handlePostNotificationPermissionResult(result: Boolean, activity: Activity?, onEvent: (UiEvent) -> Unit) {
  when {
    result -> onEvent(UiEvent.RequestPostNotificationsPermissionResult.GRANTED)
    activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false ->
      onEvent(UiEvent.RequestPostNotificationsPermissionResult.PERMANENTLY_DECLINED)

    else -> onEvent(UiEvent.RequestPostNotificationsPermissionResult.DECLINED)
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  MobileAuthenticationWithPushEnrollmentScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {}
  )
}

@Preview(showBackground = true)
@Composable
private fun PreviewAlertDialog() {
  ShowPermissionSettingsAlertDialog { }
}
