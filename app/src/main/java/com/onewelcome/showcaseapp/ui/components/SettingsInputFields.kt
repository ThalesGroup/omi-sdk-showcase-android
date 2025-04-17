package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.ui.theme.Dimensions
import kotlinx.coroutines.launch

@Composable
fun NumberSettingTextField(
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
      value = value?.toString() ?: "",
      onValueChange = { if (it.isDigitsOnly()) onValueChange.invoke(it.toIntOrNull()) },
      label = label,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    tooltipContent?.let {
      Tooltip(tooltipContent)
    }
  }
}

@Composable
fun SettingCheckbox(
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
      Tooltip(tooltipContent)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Tooltip(
  tooltipContent: @Composable () -> Unit
) {
  val tooltipState = rememberTooltipState(isPersistent = true)
  val scope = rememberCoroutineScope()
  TooltipBox(
    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
    tooltip = {
      RichTooltip(
        text = {
          Box(
            modifier = Modifier.padding(Dimensions.sPadding),
          ) {
            tooltipContent.invoke()
          }
        }
      )
    },
    state = tooltipState
  ) {
    IconButton(onClick = { scope.launch { tooltipState.show() } }) {
      Icon(
        imageVector = Icons.Filled.Info,
        contentDescription = stringResource(R.string.details)
      )
    }
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
      label = { Text("Label") },
      tooltipContent = { Text("This is tooltip content") }
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SettingsCheckBoxPreview() {
  Column {
    SettingCheckbox(
      text = "Label",
      checked = true,
      onCheckedChange = {}
    )
    SettingCheckbox(
      modifier = Modifier.fillMaxWidth(),
      text = "Label2",
      checked = true,
      onCheckedChange = {},
      tooltipContent = { Text("This is tooltip content") }
    )
  }
}
