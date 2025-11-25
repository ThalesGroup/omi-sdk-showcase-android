package com.onewelcome.core.usecase

import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onewelcome.core.entity.BiometricAuthenticatorStatus
import com.onewelcome.core.facade.BiometricFacade
import javax.inject.Inject

class GetBiometricAuthenticatorStatusUseCase @Inject constructor(private val biometricFacade: BiometricFacade) {

  fun execute(availableAuthenticatorsForUser: Set<OneginiAuthenticator>): BiometricAuthenticatorStatus {
    val isOneginiBiometricAuthenticatorAvailable = availableAuthenticatorsForUser.any { it.type == OneginiAuthenticator.Type.BIOMETRIC }
    val isBiometricReaderAvailable = biometricFacade.isBiometricReaderAvailable()
    return when {
      isOneginiBiometricAuthenticatorAvailable -> BiometricAuthenticatorStatus.AVAILABLE
      isBiometricReaderAvailable -> BiometricAuthenticatorStatus.BIOMETRICS_NOT_ENROLLED
      else -> BiometricAuthenticatorStatus.READER_NOT_PRESENT
    }
  }
}
