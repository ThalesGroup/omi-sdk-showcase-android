package com.onewelcome.showcaseapp.feature.resourcecall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.components.SdkFeatureScreen
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.theme.Dimensions

@Composable
fun ResourceCallScreen(
    navController: NavController,
    viewModel: ResourceCallViewModel = hiltViewModel()
) {
    ResourceCallScreenContent(
        uiState = viewModel.uiState,
        onNavigateBack = { navController.popBackStack() },
        onEvent = { viewModel.onEvent(it) }
    )
}

@Composable
private fun ResourceCallScreenContent(
    uiState: ResourceCallViewModel.State,
    onNavigateBack: () -> Unit,
    onEvent: (ResourceCallViewModel.UiEvent) -> Unit
) {
    SdkFeatureScreen(
        title = "Resource Calls",
        onNavigateBack = onNavigateBack,
        description = {
            ShowcaseFeatureDescription(
                description = "Demonstrates all three types of resource calls: " +
                        "Unauthenticated (no token), Anonymous (device token), " +
                        "and User Authenticated (user token).",
                link = "https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-using/android-sdk-performing-resource-calls/"
            )
        },
        settings = { ResourceTypeSelector(uiState, onEvent) },
        result = if (uiState.result != null || uiState.errorMessage != null || uiState.currentStep != null) {
            { ResultSection(uiState) }
        } else null,
        action = { ExecuteButton(uiState, onEvent) }
    )
}

@Composable
private fun ResourceTypeSelector(
    uiState: ResourceCallViewModel.State,
    onEvent: (ResourceCallViewModel.UiEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
        Text(
            text = "Select Resource Call Type:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        ResourceType.entries.forEach { type ->
            ResourceTypeCard(
                type = type,
                isSelected = uiState.selectedResourceType == type,
                onClick = { onEvent(ResourceCallViewModel.UiEvent.SelectResourceType(type)) }
            )
        }
    }
}

@Composable
private fun ResourceTypeCard(
    type: ResourceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.mPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResultSection(uiState: ResourceCallViewModel.State) {
    Column {
        // Show current step if loading
        uiState.currentStep?.let { step ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(Dimensions.mPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Show success result
        uiState.result?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(Dimensions.mPadding)
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }
        }
        
        // Show error
        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF44336).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(Dimensions.mPadding)
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun ExecuteButton(
    uiState: ResourceCallViewModel.State,
    onEvent: (ResourceCallViewModel.UiEvent) -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
        onClick = {
            if (!uiState.isLoading) {
                when (uiState.selectedResourceType) {
                    ResourceType.UNAUTHENTICATED -> onEvent(ResourceCallViewModel.UiEvent.ExecuteUnauthenticatedCall)
                    ResourceType.ANONYMOUS -> onEvent(ResourceCallViewModel.UiEvent.ExecuteAnonymousCall)
                    ResourceType.USER_AUTHENTICATED -> onEvent(ResourceCallViewModel.UiEvent.ExecuteUserAuthenticatedCall)
                    ResourceType.IMPLICIT -> onEvent(ResourceCallViewModel.UiEvent.ExecuteImplicitCall)
                }
            }
        },
        enabled = !uiState.isLoading
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.height(24.dp)
            )
        } else {
            Text("Execute ${uiState.selectedResourceType.title} Call")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ResourceCallScreenContent(
        uiState = ResourceCallViewModel.State(),
        onNavigateBack = {},
        onEvent = {}
    )
}
