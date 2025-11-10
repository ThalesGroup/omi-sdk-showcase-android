package com.onewelcome.core.omisdk.handlers

import androidx.biometric.BiometricPrompt
import com.onegini.mobile.sdk.android.handlers.request.OneginiBiometricAuthenticationRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBiometricCallback
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthenticationHandler @Inject constructor() : OneginiBiometricAuthenticationRequestHandler {

  private val _startBiometricAuthenticationFlow = Channel<BiometricPrompt.CryptoObject>(Channel.BUFFERED)
  val startBiometricAuthenticationFlow = _startBiometricAuthenticationFlow.receiveAsFlow()

  var biometricCallback: OneginiBiometricCallback? = null
    private set

  override fun startAuthentication(
    userProfile: UserProfile,
    cryptoObject: BiometricPrompt.CryptoObject,
    callback: OneginiBiometricCallback
  ) {
    biometricCallback = callback
    _startBiometricAuthenticationFlow.trySend(cryptoObject)
  }

  override fun finishAuthentication() {
    biometricCallback = null
  }
}
