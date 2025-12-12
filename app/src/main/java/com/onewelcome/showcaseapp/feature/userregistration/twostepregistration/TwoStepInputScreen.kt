package com.onewelcome.showcaseapp.feature.userregistration.twostepregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toErrorResultString
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepInputViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepInputViewModel.State
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepInputViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun TwoStepInputScreen(
    navController: NavController,
    pinNavController: NavController,
    viewModel: TwoStepInputViewModel = hiltViewModel()
) {
    TwoStepInputScreenContent(
        uiState = viewModel.uiState,
        onNavigateBack = { 
            viewModel.onEvent(UiEvent.CancelRegistration)
        },
        onSubmit = { responseCode ->
            viewModel.onEvent(UiEvent.SubmitResponseCode(responseCode))
        },
        onCancel = {
            viewModel.onEvent(UiEvent.CancelRegistration)
        },
        onNavigateToPinScreen = { pinNavController.navigate(Screens.CreatePinInput.route) },
        onGoBack = { navController.popBackStack() },
        onRegistrationComplete = { navController.popBackStack() },
        navigationEvents = viewModel.navigationEvents
    )
}

@Composable
private fun TwoStepInputScreenContent(
    uiState: State,
    onNavigateBack: () -> Unit,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    onNavigateToPinScreen: () -> Unit,
    onGoBack: () -> Unit,
    onRegistrationComplete: () -> Unit,
    navigationEvents: Flow<NavigationEvent>
) {
    var responseCode by remember { mutableStateOf("") }

    ListenForNavigationEvents(navigationEvents, onNavigateToPinScreen, onGoBack, onRegistrationComplete)

    Scaffold(
        topBar = {
            ShowcaseTopBar(stringResource(R.string.two_step_input_title)) { onNavigateBack.invoke() }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.challengeCode.isEmpty()) {
                // Show loading indicator while waiting for challenge code
                LoadingContent()
            } else if (uiState.result != null) {
                // Show result
                ResultContent(uiState, onGoBack)
            } else {
                // Show challenge/response input
                ChallengeResponseContent(
                    uiState = uiState,
                    responseCode = responseCode,
                    onResponseCodeChange = { responseCode = it },
                    onSubmit = { onSubmit(responseCode) },
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun ListenForNavigationEvents(
    navigationEvents: Flow<NavigationEvent>,
    onNavigateToPinScreen: () -> Unit,
    onGoBack: () -> Unit,
    onRegistrationComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.ToPinScreen -> onNavigateToPinScreen.invoke()
                is NavigationEvent.GoBack -> onGoBack.invoke()
                is NavigationEvent.RegistrationComplete -> onRegistrationComplete.invoke()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Starting registration...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ResultContent(uiState: State, onGoBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.mPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowcaseCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.result
                    ?.onSuccess {
                        Text(
                            text = stringResource(R.string.registration_successful),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimensions.sPadding))
                        Text(stringResource(R.string.user_profile, it.first.profileId))
                    }
                    ?.onFailure {
                        Text(
                            text = it.toErrorResultString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.mPadding))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.actionButtonHeight),
            onClick = onGoBack
        ) {
            Text(stringResource(R.string.close))
        }
    }
}

@Composable
private fun ChallengeResponseContent(
    uiState: State,
    responseCode: String,
    onResponseCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.mPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
        // Challenge Code Section
        ShowcaseCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.two_step_challenge_code_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(Dimensions.sPadding))
                Text(
                    text = uiState.challengeCode,
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
                    text = stringResource(R.string.two_step_response_code_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(Dimensions.sPadding))
                OutlinedTextField(
                    value = responseCode,
                    onValueChange = onResponseCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.two_step_response_code_hint)) },
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
        ) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimensions.actionButtonHeight),
                onClick = onCancel,
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimensions.actionButtonHeight),
                onClick = onSubmit,
                enabled = responseCode.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(Dimensions.sPadding),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.submit))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoStepInputScreenLoadingPreview() {
    TwoStepInputScreenContent(
        uiState = State(isLoading = true),
        onNavigateBack = {},
        onSubmit = {},
        onCancel = {},
        onNavigateToPinScreen = {},
        onGoBack = {},
        onRegistrationComplete = {},
        navigationEvents = emptyFlow()
    )
}

@Preview(showBackground = true)
@Composable
private fun TwoStepInputScreenChallengePreview() {
    TwoStepInputScreenContent(
        uiState = State(challengeCode = "12345", isLoading = false),
        onNavigateBack = {},
        onSubmit = {},
        onCancel = {},
        onNavigateToPinScreen = {},
        onGoBack = {},
        onRegistrationComplete = {},
        navigationEvents = emptyFlow()
    )
}
