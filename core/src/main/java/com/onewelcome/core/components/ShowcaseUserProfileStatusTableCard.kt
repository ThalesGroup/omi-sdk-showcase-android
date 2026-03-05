package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.core.R
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.success

private const val USER_PROFILE_COLUMN_WEIGHT = 0.3f
private const val STATUS_COLUMNS_TOTAL_WEIGHT = 0.7f

interface ShowcaseUserProfileStatusTableCardScope {
  fun headerRow(vararg columnTitle: String)
  fun contentRow(userProfileId: String, vararg status: Boolean)
}

private class ShowcaseUserProfileStatusTableCardScopeImpl(
  content: ShowcaseUserProfileStatusTableCardScope.() -> Unit,
) : ShowcaseUserProfileStatusTableCardScope {

  val headerRow = mutableListOf<String>()
  val contentRows = mutableListOf<Pair<String, List<Boolean>>>()

  init {
    apply(content)
  }

  override fun headerRow(vararg columnTitle: String) {
    headerRow.clear()
    headerRow.addAll(columnTitle.toList())
  }

  override fun contentRow(userProfileId: String, vararg status: Boolean) {
    contentRows.add(userProfileId to status.toList())
  }
}

@Composable
fun ShowcaseUserProfileStatusTableCard(title: String, content: ShowcaseUserProfileStatusTableCardScope.() -> Unit) {
  val rememberedContent by rememberUpdatedState(content)
  val showcaseTableContent = ShowcaseUserProfileStatusTableCardScopeImpl(rememberedContent)

  ShowcaseCard {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
      Text(
        modifier = Modifier.padding(bottom = Dimensions.sPadding),
        text = title,
        style = MaterialTheme.typography.titleMedium
      )

      val statusColumnWeight = calculateStatusColumnWeight(showcaseTableContent)
      if (showcaseTableContent.headerRow.isNotEmpty()) {
        HeaderRow(statusColumnWeight, showcaseTableContent.headerRow)
      }
      showcaseTableContent.contentRows.forEach { row ->
        ContentRow(statusColumnWeight, row)
      }
    }
  }
}

private fun calculateStatusColumnWeight(showcaseTableContent: ShowcaseUserProfileStatusTableCardScopeImpl): Float {
  val statusColumnsCount = showcaseTableContent.contentRows.maxOf { it.second.size }
  return STATUS_COLUMNS_TOTAL_WEIGHT / statusColumnsCount
}

@Composable
private fun HeaderRow(statusColumnWeight: Float, headerRow: List<String>) {
  Row {
    headerRow.forEachIndexed { index, columnTitle ->
      val weight = if (index == 0) USER_PROFILE_COLUMN_WEIGHT else statusColumnWeight
      TableCell(weight) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          style = MaterialTheme.typography.titleSmall,
          text = columnTitle,
          textAlign = if (index == 0) TextAlign.Start else TextAlign.Center
        )
      }
    }
  }
}

@Composable
private fun ContentRow(statusColumnWeight: Float, row: Pair<String, List<Boolean>>) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    TableCell(USER_PROFILE_COLUMN_WEIGHT) {
      Text(
        text = row.first,
        style = MaterialTheme.typography.bodyMedium
      )
    }
    row.second.forEach { status ->
      TableCell(statusColumnWeight) {
        Icon(
          modifier = Modifier.fillMaxWidth(),
          imageVector = if (status) Icons.Default.Check else Icons.Default.Close,
          contentDescription = if (status) stringResource(R.string.content_description_yes) else stringResource(R.string.content_description_no),
          tint = if (status) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
        )
      }
    }
  }
}

@Composable
private fun RowScope.TableCell(weight: Float, cell: @Composable () -> Unit) {
  Box(modifier = Modifier.weight(weight)) {
    cell()
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  val previewData = listOf(
    "123456" to listOf(true, false),
    "QWERTY" to listOf(true, true),
    "ZXCVBN" to listOf(false, false),
  )
  ShowcaseUserProfileStatusTableCard("Test") {
    headerRow("User profile", "OTP", "Push")
    previewData.forEach {
      contentRow(it.first, *it.second.toBooleanArray())
    }
  }
}
