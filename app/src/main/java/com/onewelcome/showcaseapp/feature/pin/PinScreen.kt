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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.R.string.clear
import com.onewelcome.showcaseapp.R.string.del

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
  if (uiState.finished == true) {
    onNavigateBack.invoke()
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Header()
    PinValidationError(uiState.pinValidationError)
    PinInputSection(onEvent, uiState.maxPinLength)
    CancelButton(onEvent)
  }
}

@Composable
private fun CancelButton(onEvent: (UiEvent) -> Unit) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(Dimensions.actionButtonHeight),
    onClick = { onEvent(UiEvent.CancelPinFlow) },
  ) {
    Text(stringResource(R.string.cancel))
  }
}

@Composable
private fun PinInputSection(onEvent: (UiEvent) -> Unit, maxPinLength: Int) {
  var pin: String by remember { mutableStateOf("") }
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
    val deleteStringRes = stringResource(del)
    val clearStringRes = stringResource(clear)
    val buttons = listOf(
      listOf("1", "2", "3"),
      listOf("4", "5", "6"),
      listOf("7", "8", "9"),
      listOf(clearStringRes, "0", deleteStringRes)
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
                deleteStringRes -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                clearStringRes -> pin = ""
                else -> if (pin.length < maxPinLength) pin += label
              }

              if (pin.length == maxPinLength) {
                onEvent.invoke(UiEvent.OnPinProvided(pin.toCharArray()))
                pin = ""
              }
            },
            modifier = Modifier
              .padding(Dimensions.sPadding)
              .size(Dimensions.pinButtonSize)
          ) {
            Text(label)
          }
        }
      }
    }
  }
}

@Composable
private fun PinValidationError(error: String) {
  if (error.isNotEmpty()) {
    Text(text = error, color = Color.Red)
  }
}

@Composable
private fun Header() {
  Text(text = stringResource(R.string.enter_pin), style = MaterialTheme.typography.headlineLarge)
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
