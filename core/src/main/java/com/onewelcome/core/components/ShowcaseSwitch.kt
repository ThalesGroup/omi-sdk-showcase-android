package com.onewelcome.core.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ShowcaseSwitch(
  shouldBeChecked: Boolean,
  onCheck: (Boolean) -> Unit,
  text: String,
  tooltipContent: @Composable (() -> Unit)? = null,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text, modifier = Modifier.weight(1f))
    Switch(
      checked = shouldBeChecked,
      onCheckedChange = onCheck
    )
    tooltipContent?.let { ShowcaseTooltip(tooltipContent) }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  ShowcaseSwitch(
    true,
    {},
    "Switch me",
    { Text("Switch tooltip") }
  )
}
