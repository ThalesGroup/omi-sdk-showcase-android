//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.fidoregistration.FidoRegistrationViewModel.State
import com.onewelcome.showcaseapp.feature.userregistration.fidoregistration.FidoRegistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens

private const val TAG = "FidoRegistrationScreen"

@Composable
fun FidoRegistrationScreen(
    navController: NavController,
    pinNavController: NavController,
    viewModel: FidoRegistrationViewModel = hiltViewModel()
) {
    Log.d(TAG, "FidoRegistrationScreen composed")

    ListenForNavigationEvents(
        navigationEvents = viewModel.navigationEvents,
        onNavigateToFido2BiometricRegistration = {
            Log.d(TAG, "Navigating to Fido2BiometricRegistration screen")
            navController.navigate(Screens.Fido2BiometricRegistration.route)
        },
        onNavigateToFido2BiometricAuthentication = {
            Log.d(TAG, "Navigating to Fido2BiometricAuthentication screen")
            navController.navigate(Screens.Fido2BiometricAuthentication.route)
        },
        onNavigateToCreatePin = {
            Log.d(TAG, "Navigating to CreatePinInput screen (OMI SDK PIN creation requested)")
            pinNavController.navigate(Screens.CreatePinInput.route)
        }
    )

    FidoRegistrationScreenContent(
        uiState = viewModel.uiState,
        onNavigateBack = {
            Log.d(TAG, "Navigate back pressed")
            navController.popBackStack()
        },
        onEvent = { event ->
            Log.d(TAG, "UI event dispatched: $event")
            viewModel.onEvent(event)
        }
    )
}

@Composable
private fun ListenForNavigationEvents(
    navigationEvents: kotlinx.coroutines.flow.Flow<FidoRegistrationViewModel.NavigationEvent>,
    onNavigateToFido2BiometricRegistration: () -> Unit,
    onNavigateToFido2BiometricAuthentication: () -> Unit,
    onNavigateToCreatePin: () -> Unit,
) {
    LaunchedEffect(Unit) {
        Log.d(TAG, "Starting to collect navigation events")
        navigationEvents.collect { event ->
            Log.d(TAG, "Navigation event received: $event")
            when (event) {
                is FidoRegistrationViewModel.NavigationEvent.NavigateToFido2BiometricRegistration -> onNavigateToFido2BiometricRegistration()
                is FidoRegistrationViewModel.NavigationEvent.NavigateToFido2BiometricAuthentication -> onNavigateToFido2BiometricAuthentication()
                is FidoRegistrationViewModel.NavigationEvent.NavigateToCreatePin -> onNavigateToCreatePin()
            }
        }
    }
}

@Composable
fun FidoRegistrationScreenContent(
    uiState: State,
    onNavigateBack: () -> Unit,
    onEvent: (UiEvent) -> Unit
) {
    SdkFeatureScreen(
        title = stringResource(R.string.fido_registration),
        onNavigateBack = onNavigateBack,
        description = {
            ShowcaseFeatureDescription(
                description = stringResource(R.string.fido_registration_description),
                link = Constants.DOCUMENTATION_USER_REGISTRATION
            )
        },
        settings = { SettingsSection(uiState, onEvent) },
        result = uiState.result?.let { { RegistrationResult(uiState) } },
        action = {
            RegistrationButton(uiState.isLoading, onEvent)
            AuthenticateButton(uiState.isLoading, onEvent)
        }
    )
}

@Composable
private fun SettingsSection(uiState: State, onEvent: (UiEvent) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
        SdkInitializationSection(uiState.isSdkInitialized)
        IdentityProvidersSection(uiState, onEvent)
        ScopesSection(uiState.isSdkInitialized, onEvent)
    }
}

@Composable
private fun SdkInitializationSection(isSdkInitialized: Boolean) {
    ShowcaseStatusCard(
        title = stringResource(R.string.status_sdk_initialized),
        status = isSdkInitialized,
        tooltipContent = { Text(stringResource(R.string.sdk_needs_to_be_initialized_to_perform_registration)) }
    )
}

@Composable
private fun IdentityProvidersSection(
    uiState: State,
    onEvent: (UiEvent) -> Unit
) {
    if (uiState.identityProviders.isNotEmpty()) {
        ShowcaseCard {
            Column {
                Text(
                    text = stringResource(R.string.identity_providers),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Dimensions.mPadding)
                )
                uiState.identityProviders.forEach { identityProvider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent.invoke(UiEvent.UpdateSelectedIdentityProvider(identityProvider)) }
                            .padding(bottom = Dimensions.sPadding)
                    ) {
                        RadioButton(
                            selected = (identityProvider == uiState.selectedIdentityProvider),
                            onClick = { onEvent.invoke(UiEvent.UpdateSelectedIdentityProvider(identityProvider)) }
                        )
                        Text(
                            stringResource(
                                R.string.idp_item_label,
                                identityProvider.name,
                                identityProvider.id
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopesSection(isSdkInitialized: Boolean, onEvent: (UiEvent) -> Unit) {
    if (isSdkInitialized) {
        ShowcaseCard {
            Column {
                Text(
                    text = stringResource(R.string.registration_scopes),
                    style = MaterialTheme.typography.titleMedium,
                )
                val scopes = Constants.DEFAULT_SCOPES
                var selectedScopes by remember { mutableStateOf(Constants.DEFAULT_SCOPES) }
                Column {
                    scopes.forEach { scope ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(scope, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = selectedScopes.contains(scope),
                                onCheckedChange = { isChecked ->
                                    selectedScopes = if (isChecked) {
                                        selectedScopes + scope
                                    } else {
                                        selectedScopes - scope
                                    }
                                    onEvent.invoke(UiEvent.UpdateSelectedScopes(selectedScopes))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationResult(uiState: State) {
    uiState.result?.let { result ->
        result.onSuccess { (userProfile, customInfo) ->
            Log.d(TAG, "Showing success result: profileId=${userProfile.profileId}")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.sPadding)
            ) {
                Text(
                    text = stringResource(R.string.registration_successful),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.user_profile_id, userProfile.profileId),
                    style = MaterialTheme.typography.bodyMedium
                )
                customInfo?.let {
                    Text(
                        text = stringResource(R.string.custom_info, ""),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.actionButtonHeight * 3)
                            .verticalScroll(rememberScrollState())
                            .padding(Dimensions.sPadding)
                    ) {
                        Text(
                            text = it.data ?: "No data",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "Showing error result: ${it.message}")
            Text(
                text = it.toErrorResultString(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RegistrationButton(isLoading: Boolean, onEvent: (UiEvent) -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
        onClick = {
            Log.d(TAG, "Register button clicked")
            onEvent(UiEvent.Register)
        },
        enabled = !isLoading
    ) {
        Text(if (isLoading) "Registering..." else stringResource(R.string.register))
    }
}

@Composable
private fun AuthenticateButton(isLoading: Boolean, onEvent: (UiEvent) -> Unit) {
    androidx.compose.material3.OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
        onClick = {
            Log.d(TAG, "Authenticate button clicked")
            onEvent(UiEvent.Authenticate)
        },
        enabled = !isLoading
    ) {
        Text(if (isLoading) "Authenticating..." else stringResource(R.string.authenticate))
    }
}
//POC-END
