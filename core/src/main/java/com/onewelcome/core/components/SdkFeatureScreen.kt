package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.core.theme.Dimensions

@Composable
fun SdkFeatureScreen(
  title: String,
  onNavigateBack: () -> Unit,
  description: @Composable () -> Unit,
  settings: @Composable () -> Unit,
  result: @Composable (() -> Unit)?,
  action: @Composable () -> Unit
) {
  Scaffold(
    topBar = { ShowcaseTopBar(title, onNavigateBack) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
      ) {
        Card(modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.padding(Dimensions.mPadding)) {
            description()
          }
        }
        Box { settings() }
      }
      Column(verticalArrangement = Arrangement.spacedBy(Dimensions.mPadding)) {
        HorizontalDivider()
        ResultCard(result)
        action()
      }
    }
  }
}

@Composable
private fun ResultCard(result: @Composable (() -> Unit)?) {
  result?.let {
    Card(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      Box(modifier = Modifier.padding(Dimensions.mPadding)) {
        result()
      }
    }
  }
}

@Preview
@Composable
private fun SdkFeatureScreenPreview() {
  SdkFeatureScreen(
    title = "Feature title",
    onNavigateBack = {},
    description = { Text("Feature description") },
    settings = {
      Text("Settings section")
    },
    result = {
      Text("Results section")
    },
    action = {
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
      ) {
        Text("Invoke action")
      }
    }
  )
}
