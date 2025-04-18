package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly

@Composable
fun ShowcaseNumberTextField(
  modifier: Modifier = Modifier,
  value: Int?,
  onValueChange: (Int?) -> Unit,
  label: @Composable (() -> Unit)? = null,
  tooltipContent: @Composable (() -> Unit)? = null
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      modifier = Modifier.weight(1f),
      value = value?.toString() ?: "",
      onValueChange = { if (it.isDigitsOnly()) onValueChange.invoke(it.toIntOrNull()) },
      label = label,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
    ShowcaseNumberTextField(
      value = null,
      onValueChange = {},
      label = { Text("Label") }
    )
    ShowcaseNumberTextField(
      value = 25,
      onValueChange = {},
      label = { Text("Label") },
      tooltipContent = { Text("This is tooltip content") }
    )
  }
}
