package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.UiState

@Composable
fun TransactionConfirmationResultScreen(navController: NavController, viewModel: TransactionConfirmationViewModel) {
  TransactionConfirmationResultScreenContent(viewModel.uiState) { navController.popBackStack() }
}

@Composable
fun TransactionConfirmationResultScreenContent(uiState: UiState, popBackStack: () -> Unit) {
  Scaffold(
    topBar = { ShowcaseTopBar("Transaction result", popBackStack) }
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(horizontal = Dimensions.mPadding),
    ) {
      uiState.result
        ?.onSuccess {
          Text("Transaction accepted successfully", style = MaterialTheme.typography.titleLarge)
          Column(modifier = Modifier.padding(top = Dimensions.mPadding)) {
            Text("Custom Info", style = MaterialTheme.typography.titleMedium)
            Text(it.toString())
          }
        }
        ?.onFailure {
          Text("Transaction failed", style = MaterialTheme.typography.titleLarge)
          Column(modifier = Modifier.padding(top = Dimensions.mPadding)) {
            Text("Exception", style = MaterialTheme.typography.titleMedium)
            Text(it.toString())
          }
        }
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        Button(
          modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.actionButtonHeight),
          onClick = popBackStack
        ) {
          Text("Close")
        }
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  TransactionConfirmationResultScreenContent(UiState()) {}
}
