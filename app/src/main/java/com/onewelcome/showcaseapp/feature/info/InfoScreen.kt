package com.onewelcome.showcaseapp.feature.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseUserProfileStatusTableCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.info.InfoViewModel.MobileAuthEnrollmentState
import com.onewelcome.showcaseapp.feature.info.InfoViewModel.State

@Composable
fun InfoScreen(
  viewModel: InfoViewModel = hiltViewModel()
) {
  LaunchedEffect(Unit) {
    viewModel.updateData()
  }
  InfoScreenContent(viewModel.uiState)
}

@Composable
private fun InfoScreenContent(
  uiState: State
) {
  Scaffold(topBar = { TopBar() }) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      StatusList(uiState)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(stringResource(R.string.title_sdk_status)) })
}

@Composable
private fun StatusList(uiState: State) {
  Column(
    modifier = Modifier.padding(top = Dimensions.mPadding),
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    ShowcaseStatusCard(
      title = stringResource(R.string.status_sdk_initialized),
      status = uiState.isSdkInitialized
    )
    ShowcaseStatusCard(
      title = stringResource(R.string.user_profiles),
      description = getUserProfiles(uiState.userProfileIds)
    )
    ShowcaseStatusCard(
      title = stringResource(R.string.authenticated_profile),
      description = getAuthenticatedProfile(uiState.authenticatedUserProfileId)
    )
    UserProfilesAuthenticators(uiState.authenticatorsState)
    UserProfilesEnrolledForMobileAuth(uiState.mobileAuthenticationEnrollmentState)
    ShowcaseStatusCard(
      title = stringResource(R.string.status_post_notifications_permission),
      status = uiState.isPostNotificationPermissionGranted,
    )
  }
}

@Composable
private fun UserProfilesAuthenticators(authenticatorsState: List<InfoViewModel.AuthenticatorsState>) {
  if (authenticatorsState.isEmpty()) {
    ShowcaseStatusCard(
      title = stringResource(R.string.status_registered_authenticators),
      description = stringResource(R.string.no_user_profiles)
    )
  } else {
    val authenticatorColumns = remember(authenticatorsState) { getAuthenticatorColumns(authenticatorsState) }
    val userProfileRows = remember(authenticatorsState, authenticatorColumns) {
      getUserProfileRows(authenticatorsState, authenticatorColumns)
    }

    ShowcaseUserProfileStatusTableCard(
      title = stringResource(R.string.status_registered_authenticators)
    ) {
      headerRow(
        stringResource(R.string.label_user_profile),
        *authenticatorColumns.map { it.type.name }.toTypedArray()
      )
      userProfileRows.forEach { (userProfileId, statuses) ->
        contentRow(userProfileId, *statuses.toBooleanArray())
      }
    }
  }
}

@Composable
private fun UserProfilesEnrolledForMobileAuth(mobileAuthEnrollmentStatus: List<MobileAuthEnrollmentState>) {
  if (mobileAuthEnrollmentStatus.isEmpty()) {
    ShowcaseStatusCard(
      title = stringResource(R.string.status_mobile_authentication_enrollment),
      description = stringResource(R.string.no_user_profiles)
    )
  } else {
    ShowcaseUserProfileStatusTableCard(
      title = stringResource(R.string.status_mobile_authentication_enrollment)
    ) {
      headerRow(stringResource(R.string.label_user_profile), stringResource(R.string.label_otp), stringResource(R.string.label_push))
      mobileAuthEnrollmentStatus.forEach {
        contentRow(it.userProfileId, it.isUserEnrolledForMobileAuth, it.isUserEnrolledForMobileAuthWithPush)
      }
    }
  }
}

@Composable
private fun getUserProfiles(userProfiles: List<String>): String =
  userProfiles.takeIf { it.isNotEmpty() }?.separateItemsWithComa() ?: stringResource(R.string.no_user_profiles)

@Composable
private fun getAuthenticatedProfile(userProfile: String): String =
  userProfile.takeIf { it.isNotEmpty() } ?: stringResource(R.string.no_authenticated_user_profile)

private fun getAuthenticatorColumns(authenticatorsState: List<InfoViewModel.AuthenticatorsState>) =
  authenticatorsState
    .flatMap { it.authenticators }
    .distinctBy { it.type }
    .sortedBy { it.type.index }

private fun getUserProfileRows(
  authenticatorsState: List<InfoViewModel.AuthenticatorsState>,
  authenticatorColumns: List<OneginiAuthenticator>
) =
  authenticatorsState.map { userState ->
    val authenticatorStatuses = authenticatorColumns.map { authenticatorType ->
      userState.authenticators.find { it.type == authenticatorType.type }?.isRegistered ?: false
    }
    userState.userProfileId to authenticatorStatuses
  }

@Preview(showBackground = true)
@Composable
private fun Preview() {
  InfoScreenContent(State())
}
