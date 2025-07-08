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
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel
import com.onewelcome.showcaseapp.feature.userregistration.browserregistration.BrowserRegistrationViewModel.UiEvent

@Composable
fun PinAuthenticationScreen(
  navController: NavController,
  viewModel: BrowserRegistrationViewModel = hiltViewModel()
) {
  PinAuthenticationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    viewModel =
  )
}

@Composable
fun PinAuthenticationScreenContent(onNavigateBack: () -> Unit) {
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
      ShowcaseFeatureDescription("Add some nice description", "")
      Button(
        modifier = Modifier
          .fillMaxWidth()
          .height(Dimensions.actionButtonHeight),
        onClick = { onEvent(UiEvent.CancelRegistration) },
        enabled = isPinAuthenticationButtonEnabled
      ) {
        Text(stringResource(R.string.cancel_registration))
      }
    }
  }
}