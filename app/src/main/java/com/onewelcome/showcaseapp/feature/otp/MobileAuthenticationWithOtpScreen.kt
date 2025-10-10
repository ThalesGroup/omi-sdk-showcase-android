package com.onewelcome.showcaseapp.feature.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiState
import com.onewelcome.showcaseapp.navigation.Screens
import com.onewelcome.showcaseapp.navigation.getResult

@Composable
fun MobileAuthenticationWithOtpScreen(
  navController: NavController,
  qrCodeScannerNavController: NavController,
  viewModel: MobileAuthenticationWithOtpViewModel = hiltViewModel()
) {
  HandleNavigationResult(qrCodeScannerNavController, viewModel::onEvent)

  MobileAuthenticationWithOtpContent(
    onNavigateBack = { navController.popBackStack() },
    uiState = viewModel.uiState,
    onEvent = viewModel::onEvent,
    onNavigateToQrCodeScanner = { qrCodeScannerNavController.navigate(Screens.QrCodeScanner.route) }
  )
}

@Composable
private fun HandleNavigationResult(qrCodeScannerNavController: NavController, onEvent: (UiEvent) -> Unit) {
  LaunchedEffect(Unit) {
    qrCodeScannerNavController.getResult<String>(Constants.QR_CODE_RESULT_KEY)?.let {
      if (it.isNotEmpty()) {
        onEvent(UiEvent.UpdateOtpValue(it))
      }
    }
  }
}

@Composable
private fun MobileAuthenticationWithOtpContent(
  onNavigateBack: () -> Unit = {},
  uiState: UiState,
  onEvent: (UiEvent) -> Unit,
  onNavigateToQrCodeScanner: () -> Unit,
) {
  SdkFeatureScreen(
    title = stringResource(R.string.mobile_authentication_with_otp),
    onNavigateBack = onNavigateBack,
    description = {
      ShowcaseFeatureDescription(
        stringResource(R.string.mobile_authentication_with_otp_description),
        Constants.DOCUMENTATION_MOBILE_AUTHENTICATION_WITH_OTP
      )
    },
    settings = { SettingsSection(uiState, onEvent, onNavigateToQrCodeScanner) },
    result = null, //TODO update result
    action = { AuthenticateButton(uiState, onEvent) }
  )
}

@Composable
private fun SettingsSection(uiState: UiState, onEvent: (UiEvent) -> Unit, onNavigateToQrCodeScanner: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    SdkInitializationSection(uiState.isSdkInitialized)
    AuthenticatedUserSection(uiState.authenticatedUserProfile)
    UserEnrolledForMobileAuthSection(uiState.isUserEnrolledForMobileAuth)
    OtpCodeSection(uiState.otp, onEvent, onNavigateToQrCodeScanner)
  }
}

@Composable
private fun OtpCodeSection(otp: String, onEvent: (UiEvent) -> Unit, onNavigateToQrCodeScanner: () -> Unit) {
  ShowcaseCard {
    Column {
      Text(
        text = stringResource(R.string.mobile_authentication_with_otp_code),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = Dimensions.mPadding)
      )
      ShowcaseTextField(
        value = otp,
        onValueChange = { onEvent(UiEvent.UpdateOtpValue(it)) },
        label = {
          Text(stringResource(R.string.mobile_authentication_with_otp_code))
        },
        trailingIcon = {
          IconButton(
            onClick = { onNavigateToQrCodeScanner() }
          ) {
            Icon(
              imageVector = ImageVector.vectorResource(R.drawable.qr_code_scanner),
              contentDescription = stringResource(R.string.qr_code_scanner_content_description)
            )
          }
        },
        tooltipContent = {
          Text(stringResource(R.string.mobile_authentication_with_otp_code_tooltip))
        }
      )
    }
  }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_sdk_initialized),
    status = isSdkInitialized,
    tooltipContent = { Text(stringResource(R.string.mobile_authentication_with_otp_sdk_initialized_tooltip)) }
  )
}

@Composable
private fun AuthenticatedUserSection(authenticatedUserProfile: UserProfile?) {
  ShowcaseStatusCard(
    title = stringResource(R.string.authenticated_profile),
    description = authenticatedUserProfile?.let { stringResource(R.string.user_profile_id, it.profileId) },
    status = authenticatedUserProfile != null,
    tooltipContent = { Text(stringResource(R.string.mobile_authentication_with_otp_authenticated_user_requirement_tooltip)) }
  )
}

@Composable
private fun UserEnrolledForMobileAuthSection(isUserEnrolledForMobileAuth: Boolean) {
  ShowcaseStatusCard(
    title = stringResource(R.string.status_user_enrolled_for_mobile_authentication),
    status = isUserEnrolledForMobileAuth,
    tooltipContent = { Text(stringResource(R.string.mobile_authentication_with_otp_enrolled_tooltip)) }
  )
}

@Composable
private fun AuthenticateButton(uiState: UiState, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.AuthenticateWithOtp) }) {
    Text(stringResource(R.string.mobile_authentication_with_otp_authenticate))
  }
}

@Preview
@Composable
private fun Preview() {
  MobileAuthenticationWithOtpContent(
    onNavigateBack = {},
    uiState = UiState(),
    onEvent = {},
    onNavigateToQrCodeScanner = {}
  )
}
