package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.showcaseapp.ui.theme.Dimensions

@Composable
fun ShowcaseCheckbox(
  modifier: Modifier = Modifier,
  text: String,
  checked: Boolean,
  tooltipContent: @Composable (() -> Unit)? = null,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = modifier
      .toggleable(
        value = checked,
        role = Role.Checkbox,
        onValueChange = onCheckedChange
      )
      .padding(top = Dimensions.sPadding, bottom = Dimensions.sPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = null
    )
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      style = MaterialTheme.typography.labelLarge
    )
    tooltipContent?.let {
      ShowcaseTooltip(tooltipContent)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  Column {
    ShowcaseCheckbox(
      text = "Label",
      checked = true,
      onCheckedChange = {}
    )
    ShowcaseCheckbox(
      modifier = Modifier.fillMaxWidth(),
      text = "Label2",
      checked = true,
      onCheckedChange = {},
      tooltipContent = { Text("This is tooltip content") }
    )
  }
}
