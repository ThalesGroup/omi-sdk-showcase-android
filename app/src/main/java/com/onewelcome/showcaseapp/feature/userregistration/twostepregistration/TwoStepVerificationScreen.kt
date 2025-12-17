package com.onewelcome.showcaseapp.feature.userregistration.twostepregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import androidx.compose.runtime.LaunchedEffect
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepRegistrationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepRegistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow

@Composable
fun TwoStepVerificationScreen(
  navController: NavController, pinNavController: NavController
) {
  val parentEntry = remember(navController) {
    navController.getBackStackEntry(Screens.TwoStepRegistration.route)
  }
  val viewModel: TwoStepRegistrationViewModel = hiltViewModel(parentEntry)
  ListenForNavigationEvents(viewModel.navigationEvents, pinNavController, navController)
  TwoStepVerificationScreenContent(challengeCode = viewModel.uiState.challengeCode, onNavigateBack = {
    viewModel.onEvent(UiEvent.CancelRegistration)
    navController.popBackStack()
  }, onSubmit = { responseCode ->
    viewModel.onEvent(UiEvent.SubmitResponseCode(responseCode))
  }, onCancel = {
    viewModel.onEvent(UiEvent.CancelRegistration)
    navController.popBackStack(route = Screens.TwoStepRegistration.route, inclusive = false)
  })
}

@Composable
private fun ListenForNavigationEvents(
  navigationEvents: Flow<NavigationEvent>, pinNavController: NavController, navController: NavController
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      if (event is NavigationEvent.ToPinScreen) {
        pinNavController.navigate(Screens.CreatePinInput.route)
      } else if (event is NavigationEvent.ToTwoStepRegistraionScreen) {
        navController.popBackStack(route = Screens.TwoStepRegistration.route, inclusive = false)
      }
    }
  }
}

@Composable
private fun TwoStepVerificationScreenContent(
  challengeCode: String, onNavigateBack: () -> Unit, onSubmit: (String) -> Unit, onCancel: () -> Unit
) {
  var responseCode by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.two_step_verification_title)) { onNavigateBack.invoke() }
    }) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      // Challenge Code Section
      ShowcaseCard {
        Column(
          modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = stringResource(R.string.two_step_challenge_code_label), style = MaterialTheme.typography.titleMedium
          )
          Spacer(modifier = Modifier.height(Dimensions.sPadding))
          Text(
            text = challengeCode,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Response Code Input Section
      ShowcaseCard {
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = stringResource(R.string.two_step_response_code_label), style = MaterialTheme.typography.titleMedium
          )
          Spacer(modifier = Modifier.height(Dimensions.sPadding))
          OutlinedTextField(
            value = responseCode,
            onValueChange = { responseCode = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.two_step_response_code_hint)) },
            singleLine = true
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Action Buttons
      Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
      ) {
        Button(
          modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
          onClick = { onSubmit(responseCode) },
          enabled = responseCode.isNotBlank()
        ) {
          Text(stringResource(R.string.submit))
        }
        OutlinedButton(
          modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
          onClick = onCancel,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
          )
        ) {
          Text(stringResource(R.string.cancel))
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TwoStepVerificationScreenPreview() {
  TwoStepVerificationScreenContent(challengeCode = "12345", onNavigateBack = {}, onSubmit = {}, onCancel = {})
}
