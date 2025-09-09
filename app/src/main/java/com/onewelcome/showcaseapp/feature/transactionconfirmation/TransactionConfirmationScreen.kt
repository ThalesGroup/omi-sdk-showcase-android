package com.onewelcome.showcaseapp.feature.transactionconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toReadableDate
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TransactionConfirmationScreen(
  homeNavController: NavHostController,
  oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest,
  transactionConfirmationViewModel: TransactionConfirmationViewModel = hiltViewModel()
) {
  TransactionConfirmationScreenContent(
    onNavigateBack = { homeNavController.popBackStack() },
    oneginiMobileAuthWithPushRequest = oneginiMobileAuthWithPushRequest,
  )
}

@Composable
private fun TransactionConfirmationScreenContent(
  onNavigateBack: () -> Unit,
  oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest,
//  transactionsViewModel: TransactionConfirmationViewModel
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
        Column {
          Text("Transaction ID", style = MaterialTheme.typography.titleMedium)
          Text(oneginiMobileAuthWithPushRequest.transactionId)
        }
        Column {
          Text("Message", style = MaterialTheme.typography.titleMedium)
          Text(oneginiMobileAuthWithPushRequest.message)
        }
        Column {
          Text("Profile ID", style = MaterialTheme.typography.titleMedium)
          Text(oneginiMobileAuthWithPushRequest.userProfileId)
        }
        Column {
          Text("Timestamp", style = MaterialTheme.typography.titleMedium)
          Text(oneginiMobileAuthWithPushRequest.timestamp.toReadableDate())
        }
        Column {
          Text("Time to live in seconds", style = MaterialTheme.typography.titleMedium)
          CountdownTimer(oneginiMobileAuthWithPushRequest.timestamp, oneginiMobileAuthWithPushRequest.timeToLiveSeconds).toString()
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing),
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier.weight(1f)
        ) {
          Button(
            modifier = Modifier
              .height(Dimensions.actionButtonHeight)
              .weight(1f),
            onClick = {}
          ) {
            Text("Accept")
          }
          Button(
            modifier = Modifier
              .height(Dimensions.actionButtonHeight)
              .weight(1f),
            onClick = {}
          ) {
            Text("Deny")
          }
        }
      }
    }
  }
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
    OneginiMobileAuthWithPushRequest("transactionId", "message", "userProfile", System.currentTimeMillis(), 300)
  )
}
