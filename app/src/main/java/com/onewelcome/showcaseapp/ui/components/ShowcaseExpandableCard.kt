package com.onewelcome.showcaseapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.ui.theme.Dimensions
import com.onewelcome.showcaseapp.ui.theme.darken

private const val ARROW_POINT_DOWN_ROTATION = 0f
private const val ARROW_POINT_UP_ROTATION = 180f
private const val CONTAINER_COLOR_DARKEN_FACTOR = 0.9f

@Composable
fun ShowcaseExpandableCard(
  modifier: Modifier = Modifier,
  title: String,
  expandableContent: @Composable () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val arrowRotation by animateFloatAsState(
    targetValue = if (expanded) ARROW_POINT_UP_ROTATION else ARROW_POINT_DOWN_ROTATION
  )
  Box(
    modifier = modifier
      .background(
        color = CardDefaults.cardColors().containerColor.darken(CONTAINER_COLOR_DARKEN_FACTOR),
        shape = CardDefaults.shape
      )
  ) {
    Column {
      Surface(
        modifier = Modifier
          .clip(CardDefaults.shape)
          .clickable { expanded = !expanded },
        color = CardDefaults.cardColors().containerColor
      ) {
        Row(
          modifier = Modifier.padding(Dimensions.mPadding),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing)
        ) {
          Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
          )
          Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = stringResource(R.string.content_description_expand_collapse),
            modifier = Modifier.rotate(arrowRotation)
          )
        }
      }

      AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
          expandFrom = Alignment.Top,
          animationSpec = tween()
        ),
        exit = shrinkVertically(
          shrinkTowards = Alignment.Top,
          animationSpec = tween()
        )
      ) {
        expandableContent.invoke()
      }
    }
  }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(LoremIpsum::class) text: String) {
  ShowcaseExpandableCard(title = "Expandable Card title") {
    Text(text)
  }
}
