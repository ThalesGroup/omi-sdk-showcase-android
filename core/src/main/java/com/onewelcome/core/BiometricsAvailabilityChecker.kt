package com.onewelcome.core

import androidx.biometric.BiometricManager
import com.onewelcome.core.facade.BiometricFacade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricsAvailabilityChecker @Inject constructor(private val biometricFacade: BiometricFacade) {

  fun isBiometricReaderAvailable(): Boolean {
    return biometricFacade.isBiometricReaderAvailable().let {
      it == BiometricManager.BIOMETRIC_SUCCESS || it == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }
  }
}
