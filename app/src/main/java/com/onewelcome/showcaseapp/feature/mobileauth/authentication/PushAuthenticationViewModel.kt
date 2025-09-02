package com.onewelcome.showcaseapp.feature.mobileauth.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PushAuthenticationViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase,
  private val permissionsFacade: PermissionsFacade
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {

  }

  private fun loadInitialData() {
    val authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
    uiState = uiState.copy(
      isSdkInitialized = isSdkInitializedUseCase.execute(),
      authenticatedUserProfile = authenticatedUserProfile,
      isUserEnrolledForMobileAuth = isUserEnrolledForMobileAuth(authenticatedUserProfile),
      isUserEnrolledForMobileAuthWithPush = isUserEnrolledForMobileAuthWithPush(authenticatedUserProfile),
      isPostNotificationPermissionGranted = permissionsFacade.checkPostNotificationsPermission()
    )
  }

  private fun isUserEnrolledForMobileAuth(authenticatedUserProfile: UserProfile?): Boolean = authenticatedUserProfile?.let {
    isUserEnrolledForMobileAuthUseCase.execute(authenticatedUserProfile).get()
  } ?: false

  private fun isUserEnrolledForMobileAuthWithPush(authenticatedUserProfile: UserProfile?): Boolean = authenticatedUserProfile?.let {
    isUserEnrolledForMobileAuthWithPushUseCase.execute(authenticatedUserProfile).get()
  } ?: false

  data class State(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isUserEnrolledForMobileAuth: Boolean = false,
    val isUserEnrolledForMobileAuthWithPush: Boolean = false,
    val isPostNotificationPermissionGranted: Boolean = false,
    val requestPostNotificationsPermission: Boolean = false,
    val result: Result<Unit, Throwable>? = null,
  )

  sealed interface UiEvent {
    data object AuthenticateWithPush : UiEvent
  }
}
