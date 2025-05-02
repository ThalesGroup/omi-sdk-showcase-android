package com.onewelcome.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

//data class ShowcaseCheckboxListItem(val name: String, var isChecked: Boolean)
//
//@Composable
//fun ShowcaseCheckboxList(items: List<ShowcaseCheckboxListItem>) {
//  var selectedCheckboxes by remember { mutableStateOf(items) }
//
//  Column {
//    selectedCheckboxes.forEach { item ->
//      Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
//      ) {
//        Text(item.name, modifier = Modifier.weight(1f))
//        Checkbox(
//          checked = item.isChecked,
//          onCheckedChange = { isChecked ->
//            item.isChecked = isChecked
//            selectedCheckboxes = selectedCheckboxes.map { if (item.name == it.name) it.copy(isChecked = isChecked) else it }
//          }
//        )
//      }
//    }
//  }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewCheckBoxList() {
//  val list = listOf(
//    ShowcaseCheckboxListItem("Item 1", false),
//    ShowcaseCheckboxListItem("Item 2", true),
//  )
//  MaterialTheme {
//    ShowcaseCheckboxList(list)
//  }
//}
