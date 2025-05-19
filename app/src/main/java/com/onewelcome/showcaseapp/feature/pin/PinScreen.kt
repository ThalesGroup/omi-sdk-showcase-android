package com.onewelcome.showcaseapp.feature.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R

@Composable
fun PinScreen(
  navController: NavController,
  viewModel: PinViewModel = hiltViewModel()
) {
  PinScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    uiState = viewModel.uiState
  )
}

@Composable
fun PinScreenContent(
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  uiState: State,
) {
  var pin: String by remember { mutableStateOf("") }
  val maxPinLength = uiState.maxPinLength
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = "Enter PIN", style = MaterialTheme.typography.titleMedium)
    Row {
      repeat(maxPinLength) { index ->
        Box(
          modifier = Modifier
            .padding(Dimensions.sPadding)
            .size(Dimensions.mPadding)
            .background(
              if (index < pin.length) Color.Black else Color.Gray,
              shape = CircleShape
            )
        )
      }
    }

    Column {
      val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("Clear", "0", "Del")
      )

      buttons.forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          row.forEach { label ->
            Button(
              onClick = {
                when (label) {
                  "Del" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                  "Clear" -> pin = ""
                  else -> if (pin.length < maxPinLength) pin += label
                }

                if (pin.length == maxPinLength) {
                  onEvent(UiEvent.OnPinProvided(pin.toCharArray()))
                }
              },
              modifier = Modifier
                .padding(8.dp)
                .size(96.dp)
            ) {
              Text(label)
            }
          }
        }
      }

      Button(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = Dimensions.mPadding)
          .height(Dimensions.actionButtonHeight),
        onClick = {
          onEvent(UiEvent.CancelPinFlow)
        },
      ) {
        Text(stringResource(R.string.cancel))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
  PinScreenContent(
    onNavigateBack = {},
    onEvent = {},
    uiState = State()
  )
}
