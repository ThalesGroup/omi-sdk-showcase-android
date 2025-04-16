package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R

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
    topBar = { TopBar(title, onNavigateBack) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.standardPadding, end = Dimensions.standardPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
      ) {
        Card(modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.padding(Dimensions.standardPadding)) {
            description()
          }
        }
        Box { settings() }
        result?.let {
          Text(
            text = stringResource(R.string.result),
            style = MaterialTheme.typography.titleSmall)
          Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(Dimensions.standardPadding)) {
              result()
            }
          }
        }
      }
      Box { action() }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String, onNavigateBack: () -> Unit) {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(title) },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.content_description_navigate_back)
        )
      }
    })
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
