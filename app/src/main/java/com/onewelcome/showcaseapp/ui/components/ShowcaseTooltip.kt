package com.onewelcome.showcaseapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.ui.theme.Dimensions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseTooltip(
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
