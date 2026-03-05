package com.onewelcome.core.omisdk.handlers

import androidx.biometric.BiometricPrompt
import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushBiometricRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiBiometricCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithBiometricRequestHandler @Inject constructor() : OneginiMobileAuthWithPushBiometricRequestHandler {
  var biometricCallback: OneginiBiometricCallback? = null
    private set

  private val _startBiometricAuthenticationFlow = Channel<BiometricPrompt.CryptoObject>(Channel.BUFFERED)
  val startBiometricAuthenticationFlow = _startBiometricAuthenticationFlow.receiveAsFlow()

  override fun startAuthentication(
    mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
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
