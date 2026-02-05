package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
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

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (uiState.isRegistrationMode) {
              "Set Custom Auth Password"
            } else {
              "Enter Custom Auth Password"
            }
          )
        },
        navigationIcon = {
          IconButton(onClick = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.CancelClicked) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
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
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = if (uiState.isRegistrationMode) {
          "Create a password for custom authentication.\nThis password will be used to authenticate you when using the custom authenticator."
        } else {
          "Enter your custom authentication password to continue."
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 24.dp)
      )

      OutlinedTextField(
        value = uiState.password,
        onValueChange = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.PasswordChanged(it)) },
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        isError = uiState.errorMessage != null,
        modifier = Modifier.fillMaxWidth()
      )

      if (uiState.isRegistrationMode) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = uiState.confirmPassword,
          onValueChange = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.ConfirmPasswordChanged(it)) },
          label = { Text("Confirm Password") },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          isError = uiState.errorMessage != null,
          modifier = Modifier.fillMaxWidth()
        )
      }

      if (uiState.errorMessage != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = uiState.errorMessage!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Button(
        onClick = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.SubmitClicked) },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(if (uiState.isRegistrationMode) "Register" else "Authenticate")
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = { viewModel.onEvent(CustomAuthPasswordViewModel.UiEvent.CancelClicked) },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Cancel")
      }
    }
  }
}
