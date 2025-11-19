package com.onewelcome.core.facade

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricFacadeImpl @Inject constructor(@param:ApplicationContext private val context: Context) : BiometricFacade {

  override fun isBiometricReaderAvailable(): Boolean {
    return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
      .let { it == BiometricManager.BIOMETRIC_SUCCESS || it == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED }
  }
}
