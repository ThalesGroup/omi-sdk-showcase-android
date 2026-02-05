package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.onewelcome.core.R
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions

@Composable
fun CustomAuthPasswordScreen(
  navController: NavHostController,
  isRegistrationMode: Boolean = true,
  viewModel: CustomAuthPasswordViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(isRegistrationMode) {
    viewModel.setRegistrationMode(isRegistrationMode)
  }

  LaunchedEffect(Unit) {
    viewModel.navigationEvents.collect { event ->
      when (event) {
        is CustomAuthPasswordViewModel.NavigationEvent.NavigateBack -> {
          navController.popBackStack()
        }
      }
    }
  }

  CustomAuthPasswordContent(
    uiState = uiState,
    onPasswordChanged = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.PasswordChanged(it)) },
    onConfirmPasswordChanged = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.ConfirmPasswordChanged(it)) },
    onSubmitClicked = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.SubmitClicked) },
    onCancelClicked = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.CancelClicked) }
  )
}

@Composable
private fun CustomAuthPasswordContent(
  uiState: CustomAuthPasswordViewModel.UiState,
  onPasswordChanged: (String) -> Unit,
  onConfirmPasswordChanged: (String) -> Unit,
  onSubmitClicked: () -> Unit,
  onCancelClicked: () -> Unit
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(
        title = if (uiState.isRegistrationMode) {
          stringResource(R.string.custom_auth_password_title_register)
        } else {
          stringResource(R.string.custom_auth_password_title_authenticate)
        },
        onNavigateBack = onCancelClicked
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      // Description Card
      ShowcaseCard {
        Text(
          text = if (uiState.isRegistrationMode) {
            stringResource(R.string.custom_auth_password_description_register)
          } else {
            stringResource(R.string.custom_auth_password_description_authenticate)
          },
          style = MaterialTheme.typography.bodyMedium
        )
      }

      // Password Input Card
      ShowcaseCard {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
        ) {
          OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(R.string.custom_auth_password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
          )

          if (uiState.isRegistrationMode) {
            OutlinedTextField(
              value = uiState.confirmPassword,
              onValueChange = onConfirmPasswordChanged,
              label = { Text(stringResource(R.string.custom_auth_password_confirm_label)) },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              isError = uiState.errorMessage != null,
              modifier = Modifier.fillMaxWidth()
            )
          }

          if (uiState.errorMessage != null) {
            Text(
              text = uiState.errorMessage,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Action Buttons
      ActionButtons(
        isRegistrationMode = uiState.isRegistrationMode,
        onSubmitClicked = onSubmitClicked,
        onCancelClicked = onCancelClicked
      )
    }
  }
}

@Composable
private fun ActionButtons(
  isRegistrationMode: Boolean,
  onSubmitClicked: () -> Unit,
  onCancelClicked: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
  ) {
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = onSubmitClicked
    ) {
      Text(
        if (isRegistrationMode) {
          stringResource(R.string.register)
        } else {
          stringResource(R.string.authenticate)
        }
      )
    }

    OutlinedButton(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = onCancelClicked,
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.error
      )
    ) {
      Text(stringResource(R.string.cancel))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CustomAuthPasswordScreenPreviewRegister() {
  CustomAuthPasswordContent(
    uiState = CustomAuthPasswordViewModel.UiState(isRegistrationMode = true),
    onPasswordChanged = {},
    onConfirmPasswordChanged = {},
    onSubmitClicked = {},
    onCancelClicked = {}
  )
}

@Preview(showBackground = true)
@Composable
private fun CustomAuthPasswordScreenPreviewAuthenticate() {
  CustomAuthPasswordContent(
    uiState = CustomAuthPasswordViewModel.UiState(isRegistrationMode = false),
    onPasswordChanged = {},
    onConfirmPasswordChanged = {},
    onSubmitClicked = {},
    onCancelClicked = {}
  )
}
