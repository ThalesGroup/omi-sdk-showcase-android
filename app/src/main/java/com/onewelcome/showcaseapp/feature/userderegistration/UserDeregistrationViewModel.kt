package com.onewelcome.showcaseapp.feature.userderegistration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.DeregisterUserUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDeregistrationViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val deregisterUserUseCase: DeregisterUserUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.UpdateSelectedUserProfile -> uiState = uiState.copy(selectedUserProfile = event.userProfile)
      is UiEvent.DeregisterUser -> deregisterUser()
    }
  }

  private fun loadInitialData() {
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    uiState = uiState.copy(isSdkInitialized = isSdkInitialized)
    if (isSdkInitialized) {
      updateUserProfiles()
    }
  }

  private fun updateUserProfiles() {
    viewModelScope.launch {
      getUserProfilesUseCase.execute()
        .onSuccess { uiState = uiState.copy(registeredUserProfiles = it, selectedUserProfile = it.firstOrNull()) }
        .onFailure { uiState = uiState.copy(registeredUserProfiles = emptySet(), selectedUserProfile = null) }
    }
  }

  private fun deregisterUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true)
      val result = uiState.selectedUserProfile?.let {
        deregisterUserUseCase.execute(it)
      } ?: Err(IllegalArgumentException("User profile not selected"))
      uiState = uiState.copy(result = result, isLoading = false)
      updateUserProfiles()
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val selectedUserProfile: UserProfile? = null,
    val registeredUserProfiles: Set<UserProfile> = emptySet(),
    val isLoading: Boolean = false,
    val result: Result<Unit, Throwable>? = null
  )

  sealed interface UiEvent {
    data class UpdateSelectedUserProfile(val userProfile: UserProfile) : UiEvent
    data object DeregisterUser : UiEvent
  }
}
