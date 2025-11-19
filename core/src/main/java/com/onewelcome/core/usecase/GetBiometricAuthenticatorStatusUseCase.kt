package com.onewelcome.core.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.BiometricAuthenticatorStatus
import com.onewelcome.core.facade.BiometricFacade
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject

class GetBiometricAuthenticatorStatusUseCase @Inject constructor(
  private val biometricFacade: BiometricFacade,
  private val omiSdkFacade: OmiSdkFacade,
) {

  fun execute(userProfile: UserProfile): Result<BiometricAuthenticatorStatus, Throwable> {
    val isBiometricAuthAvailable = biometricFacade.isBiometricReaderAvailable()
    return if (isBiometricAuthAvailable) {
      userProfile.getBiometricAuthenticator()
        .map { authenticator ->
          authenticator?.let { BiometricAuthenticatorStatus.AVAILABLE }
            ?: BiometricAuthenticatorStatus.BIOMETRICS_NOT_ENROLLED
        }
    } else {
      Ok(BiometricAuthenticatorStatus.READER_NOT_PRESENT)
    }
  }

  private fun UserProfile.getBiometricAuthenticator(): Result<OneginiAuthenticator?, Throwable> {
    return runCatching {
      omiSdkFacade.oneginiClient.getUserClient().getAllAuthenticators(this)
        .find { it.type == OneginiAuthenticator.Type.BIOMETRIC }
    }
  }
}

