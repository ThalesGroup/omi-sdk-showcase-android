package com.onewelcome.showcaseapp.fakes

import com.onewelcome.core.facade.BiometricFacade
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricFacadeFake @Inject constructor() : BiometricFacade {

  var biometricReaderAvailable: Boolean = true

  override fun isBiometricReaderAvailable(): Boolean {
    return biometricReaderAvailable
  }
}
