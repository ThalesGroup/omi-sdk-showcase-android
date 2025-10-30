package com.onewelcome.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.onewelcome.core.theme.Dimensions

@Composable
fun ShowcaseCardWithProgressIndicator(showLoadingOverlay: Boolean, content: @Composable () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth()
  ) {
    Box {
      Box(modifier = Modifier.padding(Dimensions.mPadding)) {
        content()
      }
      if (showLoadingOverlay) {
        LoaderOverlay()
      }
    }
  }
}

@Composable
private fun BoxScope.LoaderOverlay() {
  Box(
    modifier = Modifier
      .matchParentSize()
      .clickable(false) {}
      .background(Color.Black.copy(alpha = 0.4f)),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(
      color = MaterialTheme.colorScheme.secondary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
  }
}
