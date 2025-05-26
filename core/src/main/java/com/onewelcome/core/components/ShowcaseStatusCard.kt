package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.success
import com.onewelcome.showcaseapp.R

@Composable
fun ShowcaseStatusCard(
  title: String,
  description: String? = null,
  status: Boolean? = null,
  tooltipContent: @Composable (() -> Unit)? = null
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(Dimensions.mPadding),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium
        )
        description?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
      status?.let { StatusIndicator(it) }
      tooltipContent?.let { ShowcaseTooltip(tooltipContent) }
    }
  }
}

@Composable
private fun StatusIndicator(status: Boolean) {
  val icon = if (status) Icons.Default.Check else Icons.Default.Close
  val iconColor = if (status) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
  Icon(
    imageVector = icon,
    contentDescription = stringResource(R.string.content_description_yes),
    tint = iconColor
  )
}

@Preview
@Composable
private fun Preview() {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    ShowcaseStatusCard("Status card title", null)
    ShowcaseStatusCard("Status card title", null, true)
    ShowcaseStatusCard("Status card title", "Optional description", false)
    ShowcaseStatusCard("Status card title", null, true, { Text("Test tooltip content") })
    ShowcaseStatusCard(
      "Status card title",
      "Two lines long optional description Two lines long optional description",
      false,
      { Text("Test tooltip content") })
  }
}
