package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.navigation.NavHostController
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions

@Composable
fun TransactionConfirmationScreen(homeNavController: NavHostController) {
  TransactionConfirmationScreenContent { homeNavController.popBackStack() }
}

@Composable
private fun TransactionConfirmationScreenContent(
  onNavigateBack: () -> Unit
) {
  Scaffold(
    topBar = { ShowcaseTopBar("Transaction screen", onNavigateBack) }
  ) { contentPadding ->
    Box(modifier = Modifier.padding(contentPadding)) {
      Column(
        modifier = Modifier.padding(Dimensions.mPadding)
      ) {
        Text(
          style = MaterialTheme.typography.titleLarge,
          text = "There is a new transaction!"
        )
        Column(
          modifier = Modifier.padding(top = Dimensions.mPadding),
          verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
        ) {
          Text(
            text = "TRANSACTION_ID_KEY: value"
          )
          Text(
            text = "MESSAGE_KEY: value"
          )
          Text(
            text = "PROFILE_ID_KEY: value"
          )
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing),
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier.fillMaxHeight(),
        ) {
          Button(
            modifier = Modifier
              .height(Dimensions.actionButtonHeight)
              .weight(1f),
            onClick = {}
          ) {
            Text("Accept")
          }
          Button(
            modifier = Modifier
              .height(Dimensions.actionButtonHeight)
              .weight(1f),
            onClick = {}
          ) {
            Text("Deny")
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  TransactionConfirmationScreenContent({})
}