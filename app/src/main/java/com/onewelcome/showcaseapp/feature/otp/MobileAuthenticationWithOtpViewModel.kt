package com.onewelcome.showcaseapp.feature.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.MobileAuthWithOtpRequestHandler
import com.onewelcome.core.usecase.AuthenticateWithOtpUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent.AcceptAuthRequest
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent.AuthRequestHandled
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent.AuthenticateWithOtp
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent.RejectAuthRequest
import com.onewelcome.showcaseapp.feature.otp.MobileAuthenticationWithOtpViewModel.UiEvent.UpdateOtpValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileAuthenticationWithOtpViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val authenticateWithOtpUseCase: AuthenticateWithOtpUseCase,
  private val mobileAuthWithOtpRequestHandler: MobileAuthWithOtpRequestHandler
) : ViewModel() {

  var uiState by mutableStateOf(UiState())
    private set

  init {
    loadInitialData()
    listenForAuthenticationEvents()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UpdateOtpValue -> uiState = uiState.copy(otp = event.otp)
      is AuthenticateWithOtp -> authenticateWithOtp()
      is AcceptAuthRequest -> mobileAuthWithOtpRequestHandler.acceptDenyCallback?.acceptAuthenticationRequest()
      is RejectAuthRequest -> mobileAuthWithOtpRequestHandler.acceptDenyCallback?.denyAuthenticationRequest()
      is AuthRequestHandled -> uiState = uiState.copy(mobileAuthRequestToHandle = null)
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

  private fun listenForAuthenticationEvents() {
    viewModelScope.launch {
      mobileAuthWithOtpRequestHandler.startAuthWithOtpFlow.collect {
        uiState = uiState.copy(mobileAuthRequestToHandle = it)
      }
    }
  }

  private fun isUserEnrolledForMobileAuth(authenticatedUserProfile: UserProfile?): Boolean = authenticatedUserProfile?.let {
    isUserEnrolledForMobileAuthUseCase.execute(authenticatedUserProfile).get()
  } ?: false

  private fun authenticateWithOtp() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true)
      uiState = uiState.copy(
        authenticationResult = authenticateWithOtpUseCase.execute(uiState.otp),
        isLoading = false
      )
    }
  }

  data class UiState(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isUserEnrolledForMobileAuth: Boolean = false,
    val authenticationResult: Result<Unit, Throwable>? = null,
    val isLoading: Boolean = false,
    val otp: String = "",
    val mobileAuthRequestToHandle: OneginiMobileAuthenticationRequest? = null
  )

  sealed interface UiEvent {
    data class UpdateOtpValue(val otp: String) : UiEvent
    data object AuthenticateWithOtp : UiEvent
    data object AcceptAuthRequest : UiEvent
    data object RejectAuthRequest : UiEvent
    data object AuthRequestHandled : UiEvent
  }

}
