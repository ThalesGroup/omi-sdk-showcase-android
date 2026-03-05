package com.onewelcome.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onewelcome.core.theme.Dimensions

@Composable
fun ShowcaseCard(content: @Composable () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(modifier = Modifier.padding(Dimensions.mPadding)) {
      content()
    }
  }
}
