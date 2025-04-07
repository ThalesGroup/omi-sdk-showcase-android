package com.onewelcome.showcaseapp.ui.screens.sections

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel

@Composable
fun SdkInitializationScreen(
  navController: NavController,
  viewModel: SdkInitializationViewModel = hiltViewModel()
) {
  SdkInitializationScreenContent(onInitializeSdkClicked = { viewModel.initializeOneginiSdk() })
}

@Composable
private fun SdkInitializationScreenContent(onInitializeSdkClicked: () -> Unit) {
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
        onClick = { onInitializeSdkClicked.invoke() }
      ) {
        Text(stringResource(R.string.button_initialize_sdk))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SdkInitializationScreenContent({})
}
