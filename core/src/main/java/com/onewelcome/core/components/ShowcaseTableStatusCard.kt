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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.success
import com.onewelcome.showcaseapp.R

private const val DEFAULT_COLUMN_WIDTH = 1f

interface ShowcaseTableCardScope {
  fun row(vararg cells: @Composable () -> Unit)
}

private class ShowcaseTableCardScopeImpl : ShowcaseTableCardScope {
  val rows = mutableListOf<List<@Composable () -> Unit>>()

  override fun row(vararg cells: @Composable () -> Unit) {
    rows.add(cells.toList())
  }
}

@Composable
fun ShowcaseTableStatusCard(
  title: String,
  columnWeights: List<Float>,
  content: ShowcaseTableCardScope.() -> Unit
) {
  val showcaseTableContent = remember { ShowcaseTableCardScopeImpl() }
  showcaseTableContent.content()

  ShowcaseCard {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
      Text(
        modifier = Modifier.padding(bottom = Dimensions.sPadding),
        text = title,
        style = MaterialTheme.typography.titleMedium
      )
      showcaseTableContent.rows.forEach { row ->
        TableRow(columnWeights, row)
      }
    }
  }
}

@Composable
private fun TableRow(columnWeights: List<Float>, row: List<@Composable () -> Unit>) {
  Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing)) {
    row.forEachIndexed { index, cell ->
      val weight = columnWeights.elementAtOrElse(index) { DEFAULT_COLUMN_WIDTH }
      TableCell(weight, cell)
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
    Triple("123456", true, false),
    Triple("QWERTY", true, true),
    Triple("ZXCVBN", false, false),
  )
  ShowcaseTableStatusCard("test", listOf(0.4f, 0.3f, 0.3f)) {
    row(
      { Text(style = MaterialTheme.typography.titleSmall, text = "User Profile") },
      { Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, text = "OTP") },
      { Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, text = "Push") }
    )

    previewData.forEach { rowData ->
      row(
        { Text(text = rowData.first, style = MaterialTheme.typography.bodyMedium) },
        {
          Icon(
            modifier = Modifier.fillMaxWidth(),
            imageVector = if (rowData.second) Icons.Default.Check else Icons.Default.Close,
            contentDescription = stringResource(R.string.content_description_yes),
            tint = if (rowData.second) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
          )
        },
        {
          Icon(
            modifier = Modifier.fillMaxWidth(),
            imageVector = if (rowData.third) Icons.Default.Check else Icons.Default.Close,
            contentDescription = stringResource(R.string.content_description_yes),
            tint = if (rowData.third) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
          )
        })
    }
  }
}
