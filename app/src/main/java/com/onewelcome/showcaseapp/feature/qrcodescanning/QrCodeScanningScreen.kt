package com.onewelcome.showcaseapp.feature.qrcodescanning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTextField
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.qrcodescanning.QrCodeScanningViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.qrcodescanning.QrCodeScanningViewModel.State
import com.onewelcome.showcaseapp.feature.qrcodescanning.QrCodeScanningViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import com.onewelcome.showcaseapp.navigation.getResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun QrCodeScanningScreen(
  homeNavController: NavController,
  rootNavController: NavController,
  viewModel: QrCodeScanningViewModel = hiltViewModel()
) {
  HandleNavigationResult(rootNavController, viewModel::onEvent)
  ListenForNavigationEvents(viewModel.navigationEvents) {
    homeNavController.popBackStack()
  }

  QrCodeScanningScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { homeNavController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    onNavigateToQrCodeScanner = { rootNavController.navigate(Screens.QrCodeScanner.route) }
  )
}

@Composable
private fun HandleNavigationResult(
  qrCodeScannerNavController: NavController,
  onEvent: (UiEvent) -> Unit
) {
  LaunchedEffect(Unit) {
    qrCodeScannerNavController.getResult<String>(Constants.QR_CODE_RESULT_KEY)?.let {
      if (it.isNotEmpty()) {
        onEvent(UiEvent.UpdateOtpValue(it))
      }
    }
  }
}

@Composable
private fun ListenForNavigationEvents(
  navigationEvents: Flow<NavigationEvent>,
  onNavigateBack: () -> Unit
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.NavigateBack -> onNavigateBack()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrCodeScanningScreenContent(
  uiState: State,
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  onNavigateToQrCodeScanner: () -> Unit
) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.qr_code_scanning)) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.content_description_navigate_back)
            )
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(Dimensions.mPadding),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      OtpCodeSection(uiState.otp, onEvent, onNavigateToQrCodeScanner)
      
      Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
      ) {
        ProceedButton(onEvent)
        CancelButton(onEvent)
      }
    }
  }
}

@Composable
private fun OtpCodeSection(
  otp: String,
  onEvent: (UiEvent) -> Unit,
  onNavigateToQrCodeScanner: () -> Unit
) {
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
private fun ProceedButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.ProceedWithRegistration) }
  ) {
    Text(stringResource(R.string.proceed))
  }
}

@Composable
private fun CancelButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.CancelRegistration) }
  ) {
    Text(stringResource(R.string.cancel))
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  QrCodeScanningScreenContent(
    uiState = State(),
    onNavigateBack = {},
    onEvent = {},
    onNavigateToQrCodeScanner = {}
  )
}
