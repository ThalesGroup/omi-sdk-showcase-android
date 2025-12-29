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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepRegistrationViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.userregistration.twostepregistration.TwoStepRegistrationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun TwoStepOptionalDataSubmitScreen(navController: NavController) {

    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Screens.TwoStepRegistration.route)
    }
    val viewModel: TwoStepRegistrationViewModel = hiltViewModel(parentEntry)
    TwoStepInputScreenContent(
        onNavigateBack = {
            viewModel.onEvent(UiEvent.CancelRegistration)
            navController.popBackStack()
        },
        onNavigateToTwoStepVerificationScreen = {
            navController.navigate(Screens.TwoStepVerification.route)
        },
        onSubmit = { responseCode ->
            viewModel.onEvent(UiEvent.SubmitOptionalData(responseCode))
        }, onCancel = {
            viewModel.onEvent(UiEvent.CancelRegistration)
            navController.popBackStack()
        },
        navigationEvents = viewModel.navigationEvents
    )

}

@Composable
private fun TwoStepInputScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToTwoStepVerificationScreen: () -> Unit,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    navigationEvents: Flow<NavigationEvent>
) {
    var optionalData by remember { mutableStateOf("") }
    ListenForNavigationEvent(navigationEvents, onNavigateToTwoStepVerificationScreen)
    Scaffold(
        topBar = { ShowcaseTopBar(stringResource(R.string.two_step_input_title)) { onNavigateBack.invoke() } }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.mPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
        ) {
            ShowcaseCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.two_step_optional_code_hint),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(Dimensions.sPadding))
                    OutlinedTextField(
                        value = optionalData,
                        onValueChange = { optionalData = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.two_step_input_title)) },
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ActionButtons(onSubmit, onCancel, optionalData)
        }

    }
}

@Composable
fun ActionButtons(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit, optionalData: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.actionButtonHeight),
            onClick = { onSubmit(optionalData) }
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

@Composable
fun ListenForNavigationEvent(
    navigationEvents: Flow<NavigationEvent>,
    onNavigateToTwoStepVerificationScreen: () -> Unit
) {
    LaunchedEffect(Unit) {
        navigationEvents.collect { event ->
            if (event is NavigationEvent.ToTwoStepVerificationScreen)
                onNavigateToTwoStepVerificationScreen.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoStepInputScreenPreview() {
    TwoStepInputScreenContent(
        onNavigateBack = {},
        onSubmit = {},
        onCancel = {},
        onNavigateToTwoStepVerificationScreen = {},
        navigationEvents = emptyFlow()
    )
}
