package com.onewelcome.showcaseapp.ui.screens.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.ui.components.ExpandableCard
import com.onewelcome.showcaseapp.ui.theme.Dimensions
import com.onewelcome.showcaseapp.viewmodel.SdkInitializationViewModel

@Composable
fun SdkInitializationScreen(
  navController: NavController,
  viewModel: SdkInitializationViewModel = hiltViewModel()
) {
  SdkInitializationScreenContent(
    onInitializeSdkClicked = { viewModel.initializeOneginiSdk() },
    onNavigateBack = { navController.popBackStack() })
}

@Composable
private fun SdkInitializationScreenContent(
  onInitializeSdkClicked: () -> Unit,
  onNavigateBack: () -> Unit
) {
  Scaffold(
    topBar = { TopBar(onNavigateBack) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.standardPadding, end = Dimensions.standardPadding)
    ) {
      SettingsSection(
        modifier =
          Modifier
            .weight(1f)
            .padding(bottom = Dimensions.smallPadding)
      )
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onInitializeSdkClicked.invoke() }
      ) {
        Text(stringResource(R.string.button_initialize_sdk))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(stringResource(R.string.section_title_sdk_initialization)) },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.content_description_navigate_back)
        )
      }
    })
}

@Composable
private fun SettingsSection(modifier: Modifier) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      modifier = Modifier.padding(bottom = Dimensions.smallPadding),
      text = stringResource(R.string.required),
      style = MaterialTheme.typography.titleSmall
    )
    ExpandableCard(title = stringResource(R.string.label_title_handlers)) {} //TODO To be done in scope of EXAMPLEAND-153
    Text(
      modifier = Modifier.padding(top = Dimensions.standardPadding, bottom = Dimensions.smallPadding),
      text = stringResource(R.string.optional),
      style = MaterialTheme.typography.titleSmall
    )
    ExpandableCard(
      modifier = Modifier.padding(bottom = Dimensions.smallPadding),
      title = stringResource(R.string.label_http_settings)
    ) { HttpSettings() }
    ExpandableCard(
      modifier = Modifier.padding(bottom = Dimensions.smallPadding),
      title = stringResource(R.string.label_custom_authenticators)
    ) { } //TODO To be done in scope of EXAMPLEAND-155
    ExpandableCard(
      modifier = Modifier.padding(bottom = Dimensions.smallPadding),
      title = stringResource(R.string.label_custom_identity_providers)
    ) { } //TODO To be done in scope of EXAMPLEAND-156
  }
}

@Composable
private fun HttpSettings(){

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SdkInitializationScreenContent({}, {})
}
