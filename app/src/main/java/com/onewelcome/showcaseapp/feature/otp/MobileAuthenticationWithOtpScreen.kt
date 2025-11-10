package com.onewelcome.showcaseapp.feature.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.components.ShowcaseTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiState
import com.onewelcome.showcaseapp.navigation.Screens
import com.onewelcome.showcaseapp.navigation.getResult

@Composable
fun MobileAuthenticationWithOtpScreen(
  homeNavController: NavController,
  rootNavController: NavController,
  viewModel: MobileAuthenticationWithOtpViewModel = hiltViewModel()
) {
  HandleNavigationResult(rootNavController, viewModel::onEvent)

  MobileAuthenticationWithOtpContent(
    onNavigateBack = { homeNavController.popBackStack() },
    uiState = viewModel.uiState,
    onEvent = viewModel::onEvent,
    onNavigateToQrCodeScanner = { rootNavController.navigate(Screens.QrCodeScanner.route) }
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
    result = uiState.authenticationResult?.let { { AuthenticationResult(it) } },
    action = { AuthenticateButton(uiState, onEvent) }
  )
  uiState.mobileAuthRequestToHandle?.let { request ->
    ShowAuthRequestAlertDialog(request, onEvent)
  }
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
private fun AuthenticationResult(result: Result<Unit, Throwable>) {
  Column {
    result
      .onSuccess { Text(stringResource(R.string.mobile_authentication_with_otp_success)) }
      .onFailure { Text(it.toErrorResultString()) }
  }
}

@Composable
private fun AuthenticateButton(uiState: UiState, onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.AuthenticateWithOtp) }
  ) {
    if (uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.mobile_authentication_with_otp_authenticate))
    }
  }
}

@Composable
private fun ShowAuthRequestAlertDialog(request: OneginiMobileAuthenticationRequest, onEvent: (UiEvent) -> Unit) {
  AlertDialog(
    onDismissRequest = {
      onEvent(UiEvent.AuthRequestHandled)
      onEvent(UiEvent.RejectAuthRequest)
    },
    title = {
      Text(stringResource(R.string.mobile_authentication_with_otp_request_dialog_title))
    },
    text = {
      MobileAuthRequestDetails(request)
    },
    confirmButton = {
      TextButton(
        onClick = {
          onEvent(UiEvent.AuthRequestHandled)
          onEvent(UiEvent.AcceptAuthRequest)
        }) {
        Text(stringResource(R.string.accept))
      }
    },
    dismissButton = {
      TextButton(onClick = {
        onEvent(UiEvent.AuthRequestHandled)
        onEvent(UiEvent.RejectAuthRequest)
      }) {
        Text(stringResource(R.string.reject))
      }
    }

  )
}

@Composable
private fun MobileAuthRequestDetails(request: OneginiMobileAuthenticationRequest) {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    RequestInfoRecord(stringResource(R.string.mobile_authentication_with_otp_message), request.message)
    RequestInfoRecord(stringResource(R.string.mobile_authentication_with_otp_type), request.type)
    RequestInfoRecord(stringResource(R.string.mobile_authentication_with_otp_user_profile), request.userProfile.profileId)
    RequestInfoRecord(stringResource(R.string.mobile_authentication_with_otp_transaction_id), request.transactionId)
    request.signingData?.let {
      RequestInfoRecord(stringResource(R.string.mobile_authentication_with_otp_signing_data), it)
    }
  }
}

@Composable
private fun RequestInfoRecord(title: String, value: String) {
  Column {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium
    )
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

@Preview
@Composable
private fun PreviewAlertDialog() {
  MobileAuthenticationWithOtpContent(
    onNavigateBack = {},
    uiState = UiState(
      mobileAuthRequestToHandle = OneginiMobileAuthenticationRequest(
        message = "Demo message",
        type = "otp",
        userProfile = UserProfile("QWERTY"),
        transactionId = "transaction_id",
        signingData = "signing_data"
      )
    ),
    onEvent = {},
    onNavigateToQrCodeScanner = {}
  )
}
