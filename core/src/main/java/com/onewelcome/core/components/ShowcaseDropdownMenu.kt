package com.onewelcome.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.onegini.mobile.sdk.android.model.entity.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <E : Any> ShowcaseDropdownMenu(
  label: @Composable () -> Unit,
  itemList: List<E>,
  selectedItem: E?,
  valueFormatter: (E) -> String,
  tooltipContent: @Composable (() -> Unit)? = null,
  onItemSelected: (item: E, index: Int) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Column {
    ExposedDropdownMenuBox(
      modifier = Modifier.fillMaxWidth(),
      expanded = expanded,
      onExpandedChange = { expanded = !expanded }
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextField(
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .weight(1f),
          label = label,
          value = selectedItem?.let { valueFormatter(it) } ?: "",
          onValueChange = { },
          readOnly = true,
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
          }
        )
        tooltipContent?.let { ShowcaseTooltip(it) }
      }

      ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = {
          expanded = false
        }
      ) {
        itemList.forEachIndexed { index, item ->
          DropdownMenuItem(
            text = {
              Text(valueFormatter(item))
            },
            onClick = {
              onItemSelected(item, index)
              expanded = false
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ShowcaseDropdownMenuPreview() {
  ShowcaseDropdownMenu(
    label = {
      Text("Label")
    },
    itemList = listOf(
      UserProfile("123456"),
      UserProfile("QWERTY"),
    ),
    selectedItem = UserProfile("123456"),
    valueFormatter = { it.profileId },
    tooltipContent = {
      Text(text = "Tooltip text")
    },
  ) { _, _ -> }
}
