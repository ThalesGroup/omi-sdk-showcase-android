package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly

@Composable
fun ShowcaseTextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit,
  label: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  tooltipContent: @Composable (() -> Unit)? = null
) {
  TextField(
    modifier = modifier,
    value = value,
    onValueChange = onValueChange,
    type = KeyboardType.Text,
    label = label,
    trailingIcon = trailingIcon,
    tooltipContent = tooltipContent
  )
}

@Composable
fun ShowcaseNumberTextField(
  modifier: Modifier = Modifier,
  value: Int?,
  onValueChange: (Int?) -> Unit,
  label: @Composable (() -> Unit)? = null,
  tooltipContent: @Composable (() -> Unit)? = null
) {
  TextField(
    modifier = modifier,
    value = value?.toString() ?: "",
    onValueChange = { if (it.isDigitsOnly()) onValueChange.invoke(it.toIntOrNull()) },
    type = KeyboardType.Number,
    label = label,
    tooltipContent = tooltipContent
  )
}

@Composable
private fun TextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit,
  type: KeyboardType,
  label: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  tooltipContent: @Composable (() -> Unit)? = null
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      modifier = Modifier.weight(1f),
      value = value,
      onValueChange = { onValueChange.invoke(it) },
      label = label,
      trailingIcon = trailingIcon,
      keyboardOptions = KeyboardOptions(keyboardType = type)
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
    ShowcaseTextField(
      value = "",
      onValueChange = {},
      label = { Text("Label") }
    )
    ShowcaseTextField(
      value = "Some text",
      onValueChange = {},
      label = { Text("Label") },
      tooltipContent = { Text("This is tooltip content") }
    )
    ShowcaseTextField(
      value = "Some text",
      onValueChange = {},
      label = { Text("Label") },
      trailingIcon = { Icon(imageVector = Icons.Filled.Edit, contentDescription = "") },
      tooltipContent = { Text("This is tooltip content") }
    )
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
