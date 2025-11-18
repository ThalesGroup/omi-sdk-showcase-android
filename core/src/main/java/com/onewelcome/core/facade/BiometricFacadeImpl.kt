package com.onewelcome.core.facade

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class BiometricFacadeImpl @Inject constructor(@param:ApplicationContext private val context: Context) : BiometricFacade {
  override fun isBiometricReaderAvailable(): Int {
    return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
  }
}
