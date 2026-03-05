package com.onewelcome.showcaseapp.feature.transactionconfirmation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.onewelcome.core.R
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel.UiEvent

@Composable
fun PushWithCustomAuthConfirmationScreen(
  navController: NavHostController,
  viewModel: SharedPushViewModel,
) {
  PushWithCustomAuthConfirmationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
  )
}

@Composable
private fun PushWithCustomAuthConfirmationScreenContent(
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
) {
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      ShowcaseTopBar(
        title = stringResource(R.string.custom_auth_password_title_authenticate),
        onNavigateBack = onNavigateBack
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
          text = stringResource(R.string.custom_auth_password_description_authenticate),
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
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            label = { Text(stringResource(R.string.custom_auth_password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
          )

          if (errorMessage != null) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Action Buttons
      ActionButtons(
        onSubmit = {
          if (password.isBlank()) {
            errorMessage = "Password cannot be empty"
          } else {
            onEvent(UiEvent.AcceptCustomAuth(password))
          }
        },
        onCancel = onNavigateBack,
        onFallbackToPin = { onEvent(UiEvent.FallbackToPin) }
      )
    }
  }
}

@Composable
private fun ActionButtons(
  onSubmit: () -> Unit,
  onCancel: () -> Unit,
  onFallbackToPin: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
  ) {
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = onSubmit
    ) {
      Text(stringResource(R.string.authenticate))
    }

    OutlinedButton(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = onFallbackToPin
    ) {
      Text(stringResource(R.string.custom_authenticator_fallback_to_pin))
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

@Preview(showBackground = true)
@Composable
private fun PushWithCustomAuthConfirmationScreenPreview() {
  PushWithCustomAuthConfirmationScreenContent(
    onNavigateBack = {},
    onEvent = {},
  )
}
