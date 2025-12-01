package com.onewelcome.showcaseapp.feature.transactionconfirmation

import android.content.Context
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.theme.toReadableDate
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel.BiometricEvent
import com.onewelcome.showcaseapp.feature.push.SharedPushViewModel.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TransactionConfirmationScreen(
  navController: NavHostController,
  viewModel: SharedPushViewModel,
) {
  TransactionConfirmationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onEvent = { viewModel.onEvent(it) },
    uiState = viewModel.uiState,
    biometricEvents = viewModel.biometricEvents,
  )
}

@Composable
private fun TransactionConfirmationScreenContent(
  onNavigateBack: () -> Unit,
  onEvent: (UiEvent) -> Unit,
  uiState: SharedPushViewModel.UiState,
  biometricEvents: Flow<BiometricEvent>,
) {
  ListenForBiometricEvents(onEvent, biometricEvents)
  Scaffold(
    topBar = { ShowcaseTopBar(stringResource(R.string.transaction_screen), onNavigateBack) }
  ) { contentPadding ->
    Column(
      modifier = Modifier.padding(contentPadding),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing),
        modifier = Modifier.padding(horizontal = Dimensions.mPadding)
      ) {
        TransactionInfoSection(uiState.pushRequest)
        ButtonsSection(onEvent)
      }
    }
  }
}

@Composable
private fun ListenForBiometricEvents(onEvent: (UiEvent) -> Unit, biometricEvents: Flow<BiometricEvent>) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    biometricEvents.collect {
      when (it) {
        is BiometricEvent.ShowBiometricPrompt -> showBiometricPrompt(context, it.cryptoObject, onEvent)
      }
    }
  }
}

private fun showBiometricPrompt(context: Context, cryptoObject: BiometricPrompt.CryptoObject, onEvent: (UiEvent) -> Unit) {
  val executor = ContextCompat.getMainExecutor(context)
  if (context is FragmentActivity) {
    val biometricPrompt = BiometricPrompt(context, executor, getBiometricPromptAuthenticationCallback(onEvent))
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(context.getString(R.string.biometric_prompt_title))
      .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
      .setNegativeButtonText(context.getString(R.string.biometric_prompt_negative_button))
      .build()
    biometricPrompt.authenticate(promptInfo, cryptoObject)
  }
}

private fun getBiometricPromptAuthenticationCallback(onEvent: (UiEvent) -> Unit) =
  object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      super.onAuthenticationSucceeded(result)
      onEvent(UiEvent.AcceptBiometric)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
      super.onAuthenticationError(errorCode, errString)
      onEvent(UiEvent.DeclineBiometric(errorCode))
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
      Text(stringResource(R.string.accept))
    }
    Button(
      modifier = Modifier
        .height(Dimensions.actionButtonHeight)
        .weight(1f),
      onClick = { onEvent.invoke(UiEvent.Reject) }
    ) {
      Text(stringResource(R.string.reject))
    }
  }
}

@Composable
private fun TransactionInfoSection(oneginiMobileAuthWithPushRequest: OneginiMobileAuthWithPushRequest?) {
  oneginiMobileAuthWithPushRequest?.let {
    TransactionIdSection(oneginiMobileAuthWithPushRequest.transactionId)
    MessageSection(oneginiMobileAuthWithPushRequest.message)
    ProfileIdSection(oneginiMobileAuthWithPushRequest.userProfileId)
    TimeBasedSection(oneginiMobileAuthWithPushRequest.timestamp, oneginiMobileAuthWithPushRequest.timeToLiveSeconds)
  }
}

@Composable
private fun ProfileIdSection(userProfileId: String) {
  Column {
    Text(stringResource(R.string.profile_id), style = MaterialTheme.typography.titleMedium)
    Text(userProfileId)
  }
}

@Composable
private fun MessageSection(message: String) {
  Column {
    Text(stringResource(R.string.message_content), style = MaterialTheme.typography.titleMedium)
    Text(message)
  }
}

@Composable
private fun TransactionIdSection(transactionId: String) {
  Column {
    Text(stringResource(R.string.transaction_id), style = MaterialTheme.typography.titleMedium)
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
    Text(stringResource(R.string.time_to_live_seconds), style = MaterialTheme.typography.titleMedium)
    if (timestamp != 0L) CountdownTimer(timestamp, timeToLiveSeconds).toString() else NoDataAvailableText()
  }
}

@Composable
private fun TimestampSection(timestamp: Long) {
  Column {
    Text(stringResource(R.string.timestamp), style = MaterialTheme.typography.titleMedium)
    if (timestamp != 0L) Text(timestamp.toReadableDate()) else NoDataAvailableText()
  }
}

@Composable
private fun NoDataAvailableText() {
  Text(stringResource(R.string.no_data_available))
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
    {},
    SharedPushViewModel.UiState(),
    emptyFlow(),
  )
}
