//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private const val TAG = "Fido2BiometricScreen"

/**
 * Screen shown during OMI SDK registration, after initRegistration delivers the FIDO2 challenge.
 * Automatically triggers the Thales FIDO2 SDK biometric/PIN registration.
 *
 * Flow:
 * 1. OMI SDK initRegistration → challenge stored → FidoRegistrationViewModel navigates here
 * 2. This screen reads the stored challenge JSON from FidoRegistrationChallengeHolder
 * 3. Calls Thales FIDO2 SDK → biometric/PIN prompt shown
 * 4. On success → stored OMI SDK callback called with FIDO2 response → OMI SDK resumes
 */
@Composable
fun Fido2BiometricRegistrationScreen(
    navController: NavController,
    viewModel: Fido2BiometricRegistrationViewModel = hiltViewModel()
) {
    Log.d(TAG, "Fido2BiometricRegistrationScreen composed")

    val uiState by viewModel.uiState.collectAsState()
    val challengeJson by viewModel.pendingChallengeJson.collectAsState()
    val activity = LocalContext.current as? Activity

    Log.d(TAG, "Current uiState: $uiState")
    Log.d(TAG, "Challenge JSON available: ${if (challengeJson != null) "YES (${challengeJson!!.length} chars)" else "NO"}")
    Log.d(TAG, "Activity available: ${activity != null}")

    // Auto-trigger FIDO2 SDK registration as soon as the screen appears with a challenge
    LaunchedEffect(challengeJson) {
        val json = challengeJson
        Log.d(TAG, "LaunchedEffect(challengeJson): json=${if (json != null) "PRESENT" else "NULL"}, uiState=$uiState")
        if (json != null && activity != null && uiState == Fido2BiometricRegistrationViewModel.UiState.Idle) {
            Log.d(TAG, "Triggering FIDO2 SDK registration automatically")
            viewModel.startFido2Registration(activity, json)
        } else {
            Log.w(TAG, "Skipping auto-trigger: json=${json != null}, activity=${activity != null}, uiState=$uiState")
        }
    }

    // Auto-navigate back to FidoRegistration screen on success (same as Authentication screen)
    LaunchedEffect(uiState) {
        if (uiState is Fido2BiometricRegistrationViewModel.UiState.Success) {
            Log.d(TAG, "Registration successful — navigating back to FidoRegistration screen")
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            ShowcaseTopBar(
                title = "FIDO2 Device Registration",
                onNavigateBack = {
                    Log.d(TAG, "Back pressed - dismissing and popping back stack")
                    viewModel.dismiss()
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Fido2BiometricContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onSelect = { authenticator ->
                Log.d(TAG, "Authenticator selected: ${authenticator.getName()}")
                viewModel.selectAuthenticator(authenticator)
            },
            onCancelSelection = {
                Log.d(TAG, "Authenticator selection cancelled")
                viewModel.cancelAuthenticatorSelection()
            },
            onDismiss = {
                Log.d(TAG, "Dismiss action triggered - popping back stack")
                viewModel.dismiss()
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun AuthenticatorSelectionDialog(
    state: Fido2BiometricRegistrationViewModel.UiState.AuthenticatorSelection,
    onSelect: (com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorInfo) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Select verification method",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumn {
                itemsIndexed(state.selectableAuthenticators) { index, authenticator ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(authenticator) }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = authenticator.getName(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun Fido2BiometricContent(
    uiState: Fido2BiometricRegistrationViewModel.UiState,
    modifier: Modifier = Modifier,
    onSelect: (com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorInfo) -> Unit,
    onCancelSelection: () -> Unit,
    onDismiss: () -> Unit
) {
    Log.d(TAG, "Fido2BiometricContent rendering with uiState: $uiState")

    // Show authenticator selection dialog as an overlay when needed
    if (uiState is Fido2BiometricRegistrationViewModel.UiState.AuthenticatorSelection) {
        Log.d(TAG, "Rendering AuthenticatorSelection dialog with ${uiState.selectableAuthenticators.size} options")
        AuthenticatorSelectionDialog(
            state = uiState,
            onSelect = onSelect,
            onCancel = onCancelSelection
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.mPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is Fido2BiometricRegistrationViewModel.UiState.AuthenticatorSelection -> {
                // Dialog is shown above — show a waiting indicator underneath
                Log.d(TAG, "Rendering AuthenticatorSelection background state")
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "Select a verification method...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            is Fido2BiometricRegistrationViewModel.UiState.Idle -> {
                Log.d(TAG, "Rendering Idle state")
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "Preparing FIDO2 registration...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            is Fido2BiometricRegistrationViewModel.UiState.BiometricInProgress -> {
                Log.d(TAG, "Rendering BiometricInProgress state")
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "FIDO2 Registration In Progress",
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

            is Fido2BiometricRegistrationViewModel.UiState.Success -> {
                Log.d(TAG, "Rendering Success state: ${uiState.message}")
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Dimensions.mPadding))
                Text(
                    text = "FIDO2 Registration Successful!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.sPadding))
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }

            is Fido2BiometricRegistrationViewModel.UiState.Error -> {
                Log.e(TAG, "Rendering Error state: ${uiState.message}")
                Text(
                    text = "FIDO2 Registration Failed",
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
