package com.onewelcome.showcaseapp.feature.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.core.components.ShowcaseStatusCard
import com.onewelcome.showcaseapp.feature.info.InfoViewModel.State

@Composable
fun InfoScreen(
  viewModel: InfoViewModel = hiltViewModel()
) {
  viewModel.updateStatus()
  InfoScreenContent(viewModel.uiState)
}

@Composable
private fun InfoScreenContent(
  uiState: State
) {
  Scaffold(topBar = { TopBar() }) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      StatusList(uiState)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(stringResource(R.string.title_sdk_status)) })
}

@Composable
private fun StatusList(uiState: State) {
  Column(
    modifier = Modifier.padding(top = Dimensions.mPadding)
  ) {
    ShowcaseStatusCard(
      title = stringResource(R.string.status_sdk_initialized),
      status = uiState.isSdkInitialized
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  InfoScreenContent(State())
}
