package com.onewelcome.showcaseapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.showcaseapp.ui.theme.ShowcaseAppTheme
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel

@Composable
fun SdkInitializationScreen(
  navController: NavController,
  viewModel: SdkInitializationViewModel = hiltViewModel()
) {
  ShowcaseAppTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          modifier = Modifier.wrapContentSize(),
          onClick = {
            viewModel.init()
          }) {
          Text("INIT SDK")
        }
      }
    }
  }
}
