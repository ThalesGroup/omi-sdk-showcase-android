package com.onewelcome.showcaseapp.feature.mobileauth.enrollment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileAuthenticationEnrollmentViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val enrollForMobileAuthenticationUseCase: EnrollForMobileAuthenticationUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.EnrollForMobileAuthentication -> enrollForMobileAuthentication()
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

  private fun enrollForMobileAuthentication() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      uiState = uiState.copy(
        enrollmentResult = enrollForMobileAuthenticationUseCase.execute(),
        isLoading = false
      )
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isUserEnrolledForMobileAuth: Boolean = false,
    val enrollmentResult: Result<Unit, Throwable>? = null,
    val isLoading: Boolean = false
  )

  sealed interface UiEvent {
    data object EnrollForMobileAuthentication : UiEvent
  }
}
