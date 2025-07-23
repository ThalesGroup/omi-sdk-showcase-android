package com.onewelcome.showcaseapp.feature.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.entity.MobileAuthEnrollmentStatus
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetMobileAuthenticationEnrollmentStatusUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getMobileAuthenticationEnrollmentStatusUseCase: GetMobileAuthenticationEnrollmentStatusUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateData() {
    updateIsSdkInitialized()
    viewModelScope.launch {
      updateUserProfiles()
      updateAuthenticatedUserProfile()
      updateMobileAuthEnrollmentStatus()
    }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = Ok(it.map { it.profileId }.toList())) }
      .onFailure { uiState = uiState.copy(userProfileIds = Err(Unit)) }
  }

  private fun updateIsSdkInitialized() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
  }

  private fun updateAuthenticatedUserProfile() {
    uiState = uiState.copy(
      authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
    )
  }

  private suspend fun updateMobileAuthEnrollmentStatus() {
    uiState = uiState.copy(
      mobileAuthenticationEnrollmentStatus = getMobileAuthenticationEnrollmentStatusUseCase.execute().get() ?: emptyList()
    )
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfileIds: Result<List<String>, Unit>? = null,
    val authenticatedUserProfile: UserProfile? = null,
    val mobileAuthenticationEnrollmentStatus: List<MobileAuthEnrollmentStatus> = emptyList()
  )
}
