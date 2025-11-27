package com.onewelcome.core.components

import androidx.compose.foundation.layout.Box
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
  label: @Composable (() -> Unit)? = null,
  tooltipContent: @Composable (() -> Unit)? = null,
  enabled: Boolean = true
) {
  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.weight(1f)) {
      label?.let { it() }
    }
    Switch(
      enabled = enabled,
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
    { Text("Switch me") },
    { Text("Switch tooltip") }
  )
}
