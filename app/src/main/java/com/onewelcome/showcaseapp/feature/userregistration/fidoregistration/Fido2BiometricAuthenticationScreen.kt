//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions


private const val TAG = "Fido2AuthBiometricScreen"

/**
 * Screen shown during OMI SDK FIDO2 authentication, after finishRegistration delivers the FIDO2 challenge.
 * Automatically triggers the Thales FIDO2 SDK biometric/PIN authentication.
 * Auto-closes and returns to FidoRegistration screen on success.
 *
 * Flow:
 * 1. OMI SDK finishRegistration → challenge stored → FidoRegistrationViewModel navigates here
 * 2. This screen reads the stored challenge JSON from FidoAuthenticationChallengeHolder
 * 3. Calls Thales FIDO2 SDK → biometric/PIN prompt shown
 * 4. On success → stored OMI SDK callback called with FIDO2 response + userId → OMI SDK resumes
 * 5. Screen auto-closes → returns to FidoRegistration screen
 */
@Composable
fun Fido2BiometricAuthenticationScreen(
    navController: NavController,
    viewModel: Fido2BiometricAuthenticationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val challengeJson by viewModel.pendingChallengeJson.collectAsState()
    val activity = LocalContext.current as? Activity

    // Auto-trigger FIDO2 SDK authentication as soon as the screen appears with a challenge
    LaunchedEffect(challengeJson) {
        val json = challengeJson
        if (json != null && activity != null && uiState == Fido2BiometricAuthenticationViewModel.UiState.Idle) {
            viewModel.startFido2Authentication(activity, json)
        }
    }

    // Auto-navigate back when biometric authentication completes successfully
    LaunchedEffect(uiState) {
        if (uiState is Fido2BiometricAuthenticationViewModel.UiState.Success) {
            Log.d(TAG, "Authentication successful — navigating back to FidoRegistration screen")
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            ShowcaseTopBar(
                title = "FIDO2 Biometric Authentication",
                onNavigateBack = {
                    viewModel.dismiss()
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Fido2AuthBiometricContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onDismiss = {
                viewModel.dismiss()
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun Fido2AuthBiometricContent(
    uiState: Fido2BiometricAuthenticationViewModel.UiState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.mPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is Fido2BiometricAuthenticationViewModel.UiState.Idle -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "Preparing FIDO2 authentication...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            is Fido2BiometricAuthenticationViewModel.UiState.BiometricInProgress -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "FIDO2 Authentication In Progress",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.sPadding))
                Text(
                    text = "Please complete the biometric or PIN verification when prompted on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }

            is Fido2BiometricAuthenticationViewModel.UiState.Success -> {
                // Screen will auto-close via LaunchedEffect — show brief success indicator
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "Authentication Successful!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            is Fido2BiometricAuthenticationViewModel.UiState.Error -> {
                Text(
                    text = "FIDO2 Authentication Failed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.sPadding))
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}
//POC-END
