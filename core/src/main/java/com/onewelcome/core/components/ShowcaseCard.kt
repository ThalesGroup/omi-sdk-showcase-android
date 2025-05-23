package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.onewelcome.core.theme.Dimensions

@Composable
fun ShowcaseCard(title: String, route: String, onNavigationEvent: (String) -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = Dimensions.mPadding),
    onClick = { onNavigationEvent.invoke(route) }
  ) {
    Row(
      modifier = Modifier.padding(Dimensions.mPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
    ) {
      Text(
        modifier = Modifier
          .weight(1f),
        text = title,
        style = MaterialTheme.typography.titleMedium
      )
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = Icons.AutoMirrored.Filled.KeyboardArrowRight.name,
      )
    }
  }
}