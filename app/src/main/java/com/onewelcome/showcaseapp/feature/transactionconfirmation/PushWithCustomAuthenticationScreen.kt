package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushCustomRequestHandler.CustomAuthenticationData
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel.UiEvent

@Composable
fun PushWithCustomAuthenticationScreen(
  navController: NavHostController,
  viewModel: SharedPushViewModel,
) {
  PushWithCustomAuthenticationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    customAuthData = viewModel.uiState.customAuthData,
  )
}

@Composable
private fun PushWithCustomAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  customAuthData: CustomAuthenticationData?,
) {
  var inputText by remember { mutableStateOf("") }

  Scaffold(
    topBar = { ShowcaseTopBar(stringResource(R.string.custom_authentication_title), onNavigateBack) }
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .padding(horizontal = Dimensions.mPadding)
        .fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      // Transaction Info Card
      TransactionInfoCard(customAuthData)

      Spacer(modifier = Modifier.height(Dimensions.mPadding))

      // Challenge Section
      ChallengeSection(customAuthData)

      Spacer(modifier = Modifier.height(Dimensions.mPadding))

      // Input Section
      InputSection(
        inputText = inputText,
        onInputChange = { inputText = it }
      )

      Spacer(modifier = Modifier.weight(1f))

      // Buttons Section
      ButtonsSection(
        inputText = inputText,
        onSubmit = { onEvent(UiEvent.SubmitCustomData(inputText)) },
        onCancel = { onEvent(UiEvent.RejectCustomAuth) }
      )
    }
  }
}

@Composable
private fun TransactionInfoCard(customAuthData: CustomAuthenticationData?) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier.padding(Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.sPadding)
    ) {
      Text(
        text = stringResource(R.string.transaction_screen),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      customAuthData?.let { data ->
        // User Profile
        Row {
          Text(
            text = stringResource(R.string.profile_id) + ": ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = UserProfile.default.toString(),
            style = MaterialTheme.typography.bodyMedium
          )
        }

        // Message
        data.message?.let { message ->
          Row {
            Text(
              text = stringResource(R.string.message_content) + ": ",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = message,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChallengeSection(customAuthData: CustomAuthenticationData?) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.sPadding)
  ) {
    Text(
      text = stringResource(R.string.custom_authentication_challenge),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      )
    ) {
      Text(
        text = customAuthData?.challengeData ?: stringResource(R.string.no_data_available),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(Dimensions.mPadding)
      )
    }

    // Show challenge status if available
    customAuthData?.challengeStatus?.let { status ->
      Text(
        text = "Status: $status",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun InputSection(
  inputText: String,
  onInputChange: (String) -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.sPadding)
  ) {
    Text(
      text = stringResource(R.string.custom_authentication_response_hint),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
      value = inputText,
      onValueChange = onInputChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(stringResource(R.string.custom_authentication_response_hint)) },
      singleLine = false,
      minLines = 2,
      maxLines = 4
    )
  }
}

@Composable
private fun ButtonsSection(
  inputText: String,
  onSubmit: () -> Unit,
  onCancel: () -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing),
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = Dimensions.mPadding)
  ) {
    OutlinedButton(
      modifier = Modifier
        .height(Dimensions.actionButtonHeight)
        .weight(1f),
      onClick = onCancel,
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.error
      )
    ) {
      Text(stringResource(R.string.reject))
    }

    Button(
      modifier = Modifier
        .height(Dimensions.actionButtonHeight)
        .weight(1f),
      onClick = onSubmit,
      enabled = inputText.isNotBlank()
    ) {
      Text(stringResource(R.string.accept))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PushWithCustomAuthenticationScreenPreview() {
  PushWithCustomAuthenticationScreenContent(
    onNavigateBack = {},
    onEvent = {},
    customAuthData = CustomAuthenticationData(
      message = "Please confirm the transaction",
      userProfileId = UserProfile.default,
      challengeData = "Enter the code shown on your security device",
      challengeStatus = 0
    )
  )
}

@Preview(showBackground = true)
@Composable
private fun PushWithCustomAuthenticationScreenEmptyPreview() {
  PushWithCustomAuthenticationScreenContent(
    onNavigateBack = {},
    onEvent = {},
    customAuthData = null
  )
}
