package com.onewelcome.showcaseapp.feature.pin

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.invisibleIf
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.R.string.cancel
import com.onewelcome.showcaseapp.R.string.clear
import com.onewelcome.showcaseapp.R.string.del
import com.onewelcome.showcaseapp.R.string.submit
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.NavigationEvent
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.State
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent
import com.onewelcome.showcaseapp.feature.pin.PinViewModel.UiEvent.Cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PinScreen(
  navController: NavController,
  viewModel: PinViewModel
) {
  PinScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    uiState = viewModel.uiState,
    navigationEvents = viewModel.navigationEvents
  )
}

@Composable
fun PinScreenContent(
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  uiState: State,
  navigationEvents: Flow<NavigationEvent>,
) {
  var pin: CharArray by remember { mutableStateOf(charArrayOf()) }
  ListenForNavigationEvents(onNavigateBack, navigationEvents, onEvent)
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.SpaceEvenly,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Header()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      PinAttemptCounter(uiState.authenticationAttemptCounter)
      MaxPinLength(uiState.maxPinLength)
      PinValidationError(uiState.pinValidationError)
    }
    PinInputSection(onPinChange = { pin = it }, pin = pin)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing)
    ) {
      Button(
        modifier = Modifier
          .weight(1f)
          .height(Dimensions.actionButtonHeight),
        onClick = {
          onEvent(UiEvent.Submit(pin))
          pin = charArrayOf()
        },
      ) {
        Text(stringResource(submit))
      }
      OutlinedButton(
        modifier = Modifier
          .weight(1f)
          .height(Dimensions.actionButtonHeight),
        onClick = { onEvent(Cancel) },
      ) {
        Text(stringResource(cancel))
      }
    }
  }
}

@Composable
fun MaxPinLength(maxPinLength: Int) {
  if (maxPinLength > 0) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text("Max PIN length: $maxPinLength")
    }
  }
}

@Composable
fun PinAttemptCounter(authenticationAttemptCounter: AuthenticationAttemptCounter?) {
  authenticationAttemptCounter?.let {
    Text("Max attempts: ${it.maxAttempts}")
    Text("Failed attempts: ${it.failedAttempts}", color = Color.Red)
    Text("Remaining attempts: ${it.remainingAttempts}")
  }
}

@Composable
private fun ListenForNavigationEvents(onNavigateBack: () -> Unit, navigationEvents: Flow<NavigationEvent>, onEvent: (UiEvent) -> Unit) {
  BackHandler {
    onEvent.invoke(Cancel)
  }
  LaunchedEffect(Unit) {
    navigationEvents.collect { event ->
      when (event) {
        is NavigationEvent.PopBackStack -> onNavigateBack.invoke()
      }
    }
  }
}

@Composable
private fun PinInputSection(onPinChange: (CharArray) -> Unit, pin: CharArray) {
  Box(Modifier.height(Dimensions.pinInputHeight)) {
    Row {
      pin.forEach { _ ->
        Box(
          modifier = Modifier
            .padding(Dimensions.sPadding)
            .size(Dimensions.mPadding)
            .background(Color.Black, shape = CircleShape)
        )
      }
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
                deleteStringRes -> if (pin.isNotEmpty()) onPinChange(pin.dropLast(1).toCharArray())
                clearStringRes -> onPinChange(charArrayOf())
                else -> onPinChange(pin.plus(label[0]))
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
  Text(
    modifier = Modifier
      .padding(Dimensions.sPadding)
      .invisibleIf(error.isEmpty()),
    text = error,
    color = Color.Red
  )
}

@Composable
private fun Header() {
  Text(text = stringResource(R.string.enter_pin), style = MaterialTheme.typography.headlineLarge)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview() {
  PinScreenContent(
    onNavigateBack = {},
    onEvent = {},
    uiState = State(
      maxPinLength = 3,
      pinValidationError = "Wrong PIN, try again",
      authenticationAttemptCounter = AuthenticationAttemptCounter(0, 0)
    ),
    navigationEvents = emptyFlow(),
  )
}
