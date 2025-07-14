package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication.PinAuthenticationViewModel.UiEvent

@Composable
fun PinAuthenticationScreen(
  navController: NavController,
  viewModel: PinAuthenticationViewModel = hiltViewModel()
) {
  PinAuthenticationScreenContent(
    uiState = viewModel.uiState,
    onNavigateBack = { navController.popBackStack() },
    viewModel = viewModel,
    onEvent = { viewModel.onEvent(it) },
  )
}

@Composable
private fun PinAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  viewModel: PinAuthenticationViewModel,
  uiState: PinAuthenticationViewModel.State,
  onEvent: (UiEvent) -> Unit,
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.user_registration)) { onNavigateBack.invoke() }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      SdkFeatureScreen(
        title = stringResource(R.string.pin_authentication),
        onNavigateBack = onNavigateBack,
        description = {
          ShowcaseFeatureDescription(
            description = "Add some nice description",
            link = "add a link"
          )
        },
        settings = { SettingSection() },
        result = uiState.result?.let { { PinAuthenticationResult(it) } }
      ) { }
      Button(
        modifier = Modifier
          .fillMaxWidth()
          .height(Dimensions.actionButtonHeight),
        onClick = {}
//        onClick = { onEvent(UiEvent.CancelRegistration) },
//        enabled = isPinAuthenticationButtonEnabled
      ) {
        Text(stringResource(R.string.cancel_registration))
      }
    }
  }
}

@Composable
private fun SettingSection() {
  TODO("Not yet implemented")
}

@Composable
private fun PinAuthenticationResult(result: Result<Void, Throwable>?) {
  Column {
    result
      ?.onSuccess {
        Column {
          Text("magnificent success")
        }
      }
      ?.onFailure { Text("$it") }
  }
}
