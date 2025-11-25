package com.onewelcome.showcaseapp.feature.singlesignon

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
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R

@Composable
fun SingleSignOnScreen(navController: NavController) {
  SingleSignOnScreenContent(onNavigateBack = { navController.popBackStack() })
}

@Composable
private fun SingleSignOnScreenContent(onNavigateBack: () -> Unit) {
  Scaffold(topBar = { TopBar(onNavigateBack) }) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding)
    ) {
      Text(text = "Single Sign-On Feature")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
  ShowcaseTopBar(
    title = stringResource(R.string.section_title_single_sign_on),
    onNavigateBack = onNavigateBack
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SingleSignOnScreenContent {}
}
