package com.onewelcome.showcaseapp.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.feature.transaction.TransactionsViewModel.State

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
  viewModel.init()
  TransactionScreenContent(viewModel.uiState)
}

@Composable
private fun TransactionScreenContent(uiState: State) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    ShowcaseCard {
      Text("Add some nice text on the transactions")
    }
    uiState.result
      ?.onSuccess {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
        ) {
          items(it.toList()) {
            Text(it.transactionId)
          }
        }
      }
      ?.onFailure { Text("Something went wrong") }
      ?: Text("No transactions")
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  TransactionScreenContent(State())
}