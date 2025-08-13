package com.onewelcome.showcaseapp.feature.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateData() {
    updateIsSdkInitialized()
    updateAuthenticatedUserProfile()
    viewModelScope.launch {
      updateUserProfiles()
      updateMobileAuthEnrollmentState()
    }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun updateAuthenticatedUserProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfileId = it?.profileId ?: "") }
      .onFailure { uiState = uiState.copy(authenticatedUserProfileId = "") }
  }

  private fun updateIsSdkInitialized() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
  }

  private suspend fun updateMobileAuthEnrollmentState() {
    coroutineBinding {
      getUserProfilesUseCase.execute().bind().map { userProfile ->
        MobileAuthEnrollmentState(
          userProfileId = userProfile.profileId,
          isUserEnrolledForMobileAuth = isUserEnrolledForMobileAuthUseCase.execute(userProfile).bind(),
          isUserEnrolledForMobileAuthWithPush = isUserEnrolledForMobileAuthWithPushUseCase.execute(userProfile).bind()
        )
      }
    }
      .onSuccess { uiState = uiState.copy(mobileAuthenticationEnrollmentState = it) }
      .onFailure { uiState = uiState.copy(mobileAuthenticationEnrollmentState = emptyList()) }

  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfileIds: List<String> = emptyList(),
    val authenticatedUserProfileId: String = "",
    val mobileAuthenticationEnrollmentState: List<MobileAuthEnrollmentState> = emptyList()
  )

  data class MobileAuthEnrollmentState(
    val userProfileId: String,
    val isUserEnrolledForMobileAuth: Boolean,
    val isUserEnrolledForMobileAuthWithPush: Boolean
  )
}
