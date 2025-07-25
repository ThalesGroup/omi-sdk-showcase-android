package com.onewelcome.showcaseapp.feature.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTableStatusCard
import com.onewelcome.core.entity.MobileAuthEnrollmentStatus
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.separateItemsWithComa
import com.onewelcome.core.theme.success
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.info.InfoViewModel.State

private val MOBILE_AUTH_TABLE_COLUMN_WIDTHS = listOf(0.4f, 0.3f, 0.3f)

@Composable
fun InfoScreen(
  viewModel: InfoViewModel = hiltViewModel()
) {
  viewModel.updateData()
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
    UserProfilesEnrolledForMobileAuth(uiState.mobileAuthenticationEnrollmentStatus)
  }
}

@Composable
private fun UserProfilesEnrolledForMobileAuth(mobileAuthenticationEnrollmentStatus: List<MobileAuthEnrollmentStatus>) {
  if (mobileAuthenticationEnrollmentStatus.isEmpty()) {
    ShowcaseStatusCard(
      title = stringResource(R.string.mobile_authentication),
      description = stringResource(R.string.no_user_profiles)
    )
  } else {
    ShowcaseTableStatusCard(
      title = stringResource(R.string.mobile_authentication),
      cellWeights = MOBILE_AUTH_TABLE_COLUMN_WIDTHS
    ) {
      row(
        {
          Text(
            style = MaterialTheme.typography.titleSmall,
            text = stringResource(R.string.label_user_profile)
          )
        },
        {
          Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            text = stringResource(R.string.label_otp)
          )
        },
        {
          Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            text = stringResource(R.string.label_push)
          )
        }
      )

      mobileAuthenticationEnrollmentStatus.forEach { rowData ->
        row(
          { Text(text = rowData.userProfile.profileId, style = MaterialTheme.typography.bodyMedium) },
          {
            Icon(
              modifier = Modifier.fillMaxWidth(),
              imageVector = if (rowData.isEnrolledForMobileAuth) Icons.Default.Check else Icons.Default.Close,
              contentDescription = stringResource(R.string.content_description_yes),
              tint = if (rowData.isEnrolledForMobileAuth) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
            )
          },
          {
            Icon(
              modifier = Modifier.fillMaxWidth(),
              imageVector = if (rowData.isEnrolledForMobileAuthWithPush) Icons.Default.Check else Icons.Default.Close,
              contentDescription = stringResource(R.string.content_description_yes),
              tint = if (rowData.isEnrolledForMobileAuthWithPush) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
            )
          })
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
  InfoScreenContent(State())
}
