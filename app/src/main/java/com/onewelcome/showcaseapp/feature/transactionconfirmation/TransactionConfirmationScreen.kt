package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toReadableDate
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.NavigationEvent.NavigateToTransactionResultScreen
import com.onewelcome.showcaseapp.feature.transactionconfirmation.TransactionConfirmationViewModel.UiEvent
import com.onewelcome.showcaseapp.navigation.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TransactionConfirmationScreen(
  navController: NavHostController,
  oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest,
  viewModel: TransactionConfirmationViewModel,
) {
  ListenForNavigationEvents(viewModel.navigationEvents, navController)
  TransactionConfirmationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    oneginiMobileAuthWithPushRequest = oneginiMobileAuthWithPushRequest,
    onEvent = { viewModel.onEvent(it) }
  )
}

@Composable
private fun ListenForNavigationEvents(
  navigationEvents: Flow<TransactionConfirmationViewModel.NavigationEvent>,
  navController: NavHostController
) {
  LaunchedEffect(Unit) {
    navigationEvents.collect {
      when (it) {
        NavigateToTransactionResultScreen -> navController.navigate(Screens.TransactionConfirmationResult.route) {
          popUpTo(navController.currentDestination?.id ?: return@navigate) {
            inclusive = true
          }
        }
      }
    }
  }
}

@Composable
private fun TransactionConfirmationScreenContent(
  onNavigateBack: () -> Unit,
  oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest,
  onEvent: (UiEvent) -> Unit,
) {
  Scaffold(
    topBar = { ShowcaseTopBar("Transaction screen", onNavigateBack) }
  ) { contentPadding ->
    Column(
      modifier = Modifier.padding(contentPadding),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing),
        modifier = Modifier.padding(horizontal = Dimensions.mPadding)
      ) {
        TransactionInfoSection(oneginiMobileAuthWithPushRequest)
        ButtonsSection(onEvent)
      }
    }
  }
}

@Composable
private fun ButtonsSection(onEvent: (UiEvent) -> Unit) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing),
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.fillMaxHeight()
  ) {
    Button(
      modifier = Modifier
        .height(Dimensions.actionButtonHeight)
        .weight(1f),
      onClick = { onEvent.invoke(UiEvent.Accept) }
    ) {
      Text("Accept")
    }
    Button(
      modifier = Modifier
        .height(Dimensions.actionButtonHeight)
        .weight(1f),
      onClick = { onEvent.invoke(UiEvent.Reject) }
    ) {
      Text("Reject")
    }
  }
}

@Composable
private fun TransactionInfoSection(oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest) {
  TransactionIdSection(oneginiMobileAuthWithPushRequest.transactionId)
  MessageSection(oneginiMobileAuthWithPushRequest.message)
  ProfileIdSection(oneginiMobileAuthWithPushRequest.userProfileId)
  TimeBasedSection(oneginiMobileAuthWithPushRequest.timestamp, oneginiMobileAuthWithPushRequest.timeToLiveSeconds)
}

@Composable
private fun ProfileIdSection(userProfileId: String) {
  Column {
    Text("Profile ID", style = MaterialTheme.typography.titleMedium)
    Text(userProfileId)
  }
}

@Composable
private fun MessageSection(message: String) {
  Column {
    Text("Message", style = MaterialTheme.typography.titleMedium)
    Text(message)
  }
}

@Composable
private fun TransactionIdSection(transactionId: String) {
  Column {
    Text("Transaction ID", style = MaterialTheme.typography.titleMedium)
    Text(transactionId)
  }
}

@Composable
private fun TimeBasedSection(timestamp: Long, timeToLiveSeconds: Int) {
  TimestampSection(timestamp)
  CountdownTimerSection(timestamp, timeToLiveSeconds)
}

@Composable
private fun CountdownTimerSection(timestamp: Long, timeToLiveSeconds: Int) {
  Column {
    Text("Time to live in seconds", style = MaterialTheme.typography.titleMedium)
    if (timestamp != 0L) CountdownTimer(timestamp, timeToLiveSeconds).toString() else NoDataAvailableText()
  }
}

@Composable
private fun TimestampSection(timestamp: Long) {
  Column {
    Text("Timestamp", style = MaterialTheme.typography.titleMedium)
    if (timestamp != 0L) Text(timestamp.toReadableDate()) else NoDataAvailableText()
  }
}

@Composable
private fun NoDataAvailableText() {
  Text("No data available")
}

@Composable
private fun CountdownTimer(
  timestamp: Long,
  timeToLiveInSeconds: Int,
) {
  val expirationTime = timestamp + TimeUnit.SECONDS.toMillis(timeToLiveInSeconds.toLong())
  val totalRemainingSeconds = TimeUnit.MILLISECONDS.toSeconds(expirationTime - System.currentTimeMillis()).toInt()
  var secondsLeft by remember { mutableIntStateOf(totalRemainingSeconds) }
  LaunchedEffect(totalRemainingSeconds) {
    while (secondsLeft > 0) {
      delay(1000L)
      secondsLeft--
    }
  }
  val minutes = TimeUnit.SECONDS.toMinutes(secondsLeft.toLong())
  val seconds = secondsLeft % 60
  Text(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds))
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  TransactionConfirmationScreenContent(
    {},
    OneginiMobileAuthWithPushRequest("transactionId", "message", "userProfile", System.currentTimeMillis(), 300),
    {}
  )
}
