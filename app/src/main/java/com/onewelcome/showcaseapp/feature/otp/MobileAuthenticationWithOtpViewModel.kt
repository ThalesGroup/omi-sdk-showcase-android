package com.onewelcome.showcaseapp.feature.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MobileAuthenticationWithOtpViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase
) : ViewModel() {

  var uiState by mutableStateOf(UiState())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.UpdateOtpValue -> uiState = uiState.copy(otp = event.otp)
      is UiEvent.AuthenticateWithOtp -> authenticateWithOtp()
    }
  }

  private fun loadInitialData() {
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    val authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
    uiState = uiState.copy(
      isSdkInitialized = isSdkInitialized,
      authenticatedUserProfile = authenticatedUserProfile,
      isUserEnrolledForMobileAuth = isUserEnrolledForMobileAuth(authenticatedUserProfile)
    )
  }

  private fun isUserEnrolledForMobileAuth(authenticatedUserProfile: UserProfile?): Boolean = authenticatedUserProfile?.let {
    isUserEnrolledForMobileAuthUseCase.execute(authenticatedUserProfile).get()
  } ?: false

  private fun authenticateWithOtp() {
    //todo authenticate with otp usecase
  }

  data class UiState(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isUserEnrolledForMobileAuth: Boolean = false,
    val authenticationResult: Result<Unit, Throwable>? = null,
    val isLoading: Boolean = false,
    val otp: String = ""
  )

  sealed interface UiEvent {
    data class UpdateOtpValue(val otp: String) : UiEvent
    data object AuthenticateWithOtp : UiEvent
  }
}
