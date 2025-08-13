package com.onewelcome.showcaseapp.feature.mobileauth.enrollment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.usecase.EnrollForMobileAuthenticationWithPushUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthUseCase
import com.onewelcome.core.usecase.IsUserEnrolledForMobileAuthWithPushUseCase
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.DismissSettingsDialog
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.EnrollForMobileAuthenticationWithPush
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.PostNotificationsPermissionClicked
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.RequestPostNotificationsPermissionResult
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.RequestPostNotificationsPermissionResult.DECLINED
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.RequestPostNotificationsPermissionResult.GRANTED
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.RequestPostNotificationsPermissionResult.PERMANENTLY_DECLINED
import com.onewelcome.showcaseapp.feature.mobileauth.enrollment.MobileAuthenticationWithPushEnrollmentViewModel.UiEvent.UpdatePostNotificationsPermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MobileAuthenticationWithPushEnrollmentViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val isUserEnrolledForMobileAuthUseCase: IsUserEnrolledForMobileAuthUseCase,
  private val isUserEnrolledForMobileAuthWithPushUseCase: IsUserEnrolledForMobileAuthWithPushUseCase,
  private val enrollForMobileAuthenticationWithPushUseCase: EnrollForMobileAuthenticationWithPushUseCase,
  private val permissionsFacade: PermissionsFacade
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is EnrollForMobileAuthenticationWithPush -> enrollForMobileAuthWithPush()
      is PostNotificationsPermissionClicked -> onPostNotificationsPermissionClicked(event.shouldAddPermission)
      is RequestPostNotificationsPermissionResult -> onRequestPostNotificationsPermissionResult(event)
      is DismissSettingsDialog -> uiState = uiState.copy(showSettingsDialog = false)
      is UpdatePostNotificationsPermissionState -> uiState =
        uiState.copy(isPostNotificationPermissionGranted = permissionsFacade.checkPostNotificationsPermission())
    }
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

  private fun enrollForMobileAuthWithPush() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      uiState = uiState.copy(
        enrollmentResult = enrollForMobileAuthenticationWithPushUseCase.execute(),
        isLoading = false
      )
      loadInitialData()
    }
  }

  private fun onPostNotificationsPermissionClicked(shouldAddPermission: Boolean) {
    uiState = if (shouldAddPermission) {
      uiState.copy(requestPostNotificationsPermission = true)
    } else {
      uiState.copy(showSettingsDialog = true)
    }

  }

  private fun onRequestPostNotificationsPermissionResult(result: RequestPostNotificationsPermissionResult) {
    uiState = when (result) {
      GRANTED -> uiState.copy(isPostNotificationPermissionGranted = true)
      DECLINED -> uiState.copy(isPostNotificationPermissionGranted = false)
      PERMANENTLY_DECLINED -> uiState.copy(showSettingsDialog = true, isPostNotificationPermissionGranted = false)
    }.copy(requestPostNotificationsPermission = false)
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isUserEnrolledForMobileAuth: Boolean = false,
    val isUserEnrolledForMobileAuthWithPush: Boolean = false,
    val isPostNotificationPermissionGranted: Boolean = false,
    val enrollmentResult: Result<Unit, Throwable>? = null,
    val isLoading: Boolean = false,
    val requestPostNotificationsPermission: Boolean = false,
    val showSettingsDialog: Boolean = false
  )

  sealed interface UiEvent {
    data object EnrollForMobileAuthenticationWithPush : UiEvent
    data class PostNotificationsPermissionClicked(val shouldAddPermission: Boolean) : UiEvent
    enum class RequestPostNotificationsPermissionResult : UiEvent {
      GRANTED, DECLINED, PERMANENTLY_DECLINED
    }
    data object DismissSettingsDialog : UiEvent
    data object UpdatePostNotificationsPermissionState : UiEvent
  }
}
