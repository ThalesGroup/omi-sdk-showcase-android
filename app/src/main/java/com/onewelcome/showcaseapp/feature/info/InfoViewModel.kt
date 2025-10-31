package com.onewelcome.showcaseapp.feature.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
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
  private val isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase,
  private val getAuthenticatorsUseCase: GetAuthenticatorsUseCase,
  private val permissionsFacade: PermissionsFacade
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateData() {
    updateIsSdkInitialized()
    updateAuthenticatedUserProfile()
    updatePostNotificationPermissionStatus()
    viewModelScope.launch {
      with(getUserProfilesUseCase.execute()) {
        updateUserProfilesState()
        updateAuthenticatorsState()
        updateMobileAuthEnrollmentState()
      }
    }
  }

  private fun updateIsSdkInitialized() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
  }

  private fun updateAuthenticatedUserProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfileId = it?.profileId ?: "") }
      .onFailure { uiState = uiState.copy(authenticatedUserProfileId = "") }
  }

  private fun updatePostNotificationPermissionStatus() {
    uiState = uiState.copy(isPostNotificationPermissionGranted = permissionsFacade.checkPostNotificationsPermission())
  }

  private fun Result<Set<UserProfile>, Throwable>.updateUserProfilesState() {
    this.onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun Result<Set<UserProfile>, Throwable>.updateAuthenticatorsState() {
    binding {
      bind().map { userProfile ->
        AuthenticatorsState(
          userProfileId = userProfile.profileId,
          authenticators = getAuthenticatorsUseCase.execute(userProfile).bind()
        )
      }
    }
      .onSuccess { uiState = uiState.copy(authenticatorsState = it) }
      .onFailure { uiState = uiState.copy(authenticatorsState = emptyList()) }
  }

  private fun Result<Set<UserProfile>, Throwable>.updateMobileAuthEnrollmentState() {
    binding {
      bind().map { userProfile ->
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
    val authenticatorsState: List<AuthenticatorsState> = emptyList(),
    val mobileAuthenticationEnrollmentState: List<MobileAuthEnrollmentState> = emptyList(),
    val isPostNotificationPermissionGranted: Boolean = false
  )

  data class AuthenticatorsState(
    val userProfileId: String,
    val authenticators: Set<OneginiAuthenticator>
  )

  data class MobileAuthEnrollmentState(
    val userProfileId: String,
    val isUserEnrolledForMobileAuth: Boolean,
    val isUserEnrolledForMobileAuthWithPush: Boolean
  )
}
