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

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.LoadInitialData -> loadInitialData()
      is UiEvent.OnUserProfileSelected -> uiState = uiState.copy(selectedUserProfile = event.userProfile)
      is UiEvent.DeregisterUser -> deregisterUser()
    }
  }

  private fun loadInitialData() {
    isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
    loadUserProfiles()
  }

  private fun loadUserProfiles() {
    viewModelScope.launch {
      getUserProfilesUseCase.execute()
        .onSuccess { uiState = uiState.copy(registeredUserProfiles = it, selectedUserProfile = it.firstOrNull()) }
        .onFailure { uiState = uiState.copy(registeredUserProfiles = emptySet(), selectedUserProfile = null) }
    }
  }

  private fun deregisterUser() {
    viewModelScope.launch {
      val result = uiState.selectedUserProfile?.let {
        deregisterUserUseCase.execute(it)
      } ?: Err(IllegalArgumentException("User profile not selected"))
      uiState = uiState.copy(isLoading = true)
      uiState = uiState.copy(result = result, isLoading = false)
      loadUserProfiles()
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
    data object LoadInitialData : UiEvent
    data class OnUserProfileSelected(val userProfile: UserProfile) : UiEvent
    data object DeregisterUser : UiEvent
  }
}
