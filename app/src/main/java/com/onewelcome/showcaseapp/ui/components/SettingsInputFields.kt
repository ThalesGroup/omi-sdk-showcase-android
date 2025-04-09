package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.onewelcome.showcaseapp.ui.theme.Dimensions

@Composable
fun NumberSettingTextField(
  modifier: Modifier = Modifier,
  value: Int?,
  onValueChange: (Int?) -> Unit,
  label: @Composable (() -> Unit)? = null
) {
  OutlinedTextField(
    modifier = modifier,
    value = value?.toString() ?: "",
    onValueChange = { if (it.isDigitsOnly()) onValueChange.invoke(it.toIntOrNull()) },
    label = label,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
  )
}

@Composable
fun SettingCheckbox(modifier: Modifier = Modifier, text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = modifier
      .toggleable(
        value = checked,
        role = Role.Checkbox,
        onValueChange = onCheckedChange
      )
      .padding(top = Dimensions.smallPadding, bottom = Dimensions.smallPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing)
  ) {
    Checkbox(
      modifier = Modifier.padding(start = 0.dp),
      checked = checked,
      onCheckedChange = null
    )
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      style = MaterialTheme.typography.labelLarge
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun NumberSettingTextFieldPreview() {
  Column {
    NumberSettingTextField(
      value = null,
      onValueChange = {},
      label = { Text("Label") }
    )
    NumberSettingTextField(
      value = 25,
      onValueChange = {},
      label = { Text("Label") }
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SettingsCheckBoxPreview() {
  SettingCheckbox(
    text = "Label",
    checked = true,
    onCheckedChange = {}
  )
}
