package com.onewelcome.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onewelcome.core.theme.Dimensions

@Composable
fun OsCompatibilityScreen(navController: NavController) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
    ) {
      Text("Info Screen")
    }
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
    ) { }
  }
}
