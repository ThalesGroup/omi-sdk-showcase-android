package com.onewelcome.showcaseapp.feature.userauthentication.implicitauthentication

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
import com.onewelcome.core.usecase.GetImplicitlyAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.ImplicitAuthenticationUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImplicitAuthenticationViewModel @Inject() constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val implicitAuthenticationUseCase: ImplicitAuthenticationUseCase,
  private val getImplicitlyAuthenticatedUserProfileUseCase: GetImplicitlyAuthenticatedUserProfileUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  init {
    loadData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartImplicitAuthentication -> startImplicitAuthentication()
      is UiEvent.UpdateSelectedUserProfile -> updateSelectedUserProfile(event)
      is UiEvent.UpdateSelectedScopes -> uiState = uiState.copy(selectedScopes = event.scopes)
    }
  }

  private fun updateSelectedUserProfile(event: UiEvent.UpdateSelectedUserProfile) {
    uiState = uiState.copy(selectedUserProfile = event.userProfile)
  }

  private fun loadData() {
    updateIsSdkInitialized()
    updateAuthenticatedProfile()
    viewModelScope.launch {
      updateUserProfiles()
      updateAuthenticateButton()
    }
  }

  private fun updateAuthenticatedProfile() {
    getImplicitlyAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(implicitlyAuthenticatedUserProfile = it) }
      .onFailure { uiState = uiState.copy(implicitlyAuthenticatedUserProfile = null) }
  }

  private fun updateIsSdkInitialized() {
    isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
  }

  private fun updateAuthenticateButton() {
    uiState = uiState.copy(isAuthenticateButtonEnabled = uiState.isSdkInitialized && uiState.selectedUserProfile != null)
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfiles = it, selectedUserProfile = it.firstOrNull()) }
      .onFailure { uiState = uiState.copy(userProfiles = emptySet()) }
  }

  private fun startImplicitAuthentication() {
    authenticateUser()
  }

  private fun authenticateUser() {
    uiState.selectedUserProfile?.let { selectedUserProfile ->
      viewModelScope.launch {
        implicitAuthenticationUseCase.execute(selectedUserProfile, uiState.selectedScopes.toTypedArray())
          .onSuccess { uiState = uiState.copy(implicitlyAuthenticatedUserProfile = it) }
          .also { uiState = uiState.copy(result = it) }
      }
    } ?: run {
      uiState = uiState.copy(result = Err(IllegalArgumentException("User profile not selected")))
    }
  }

  data class State(
    val result: Result<UserProfile, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
    val userProfiles: Set<UserProfile> = emptySet(),
    val selectedUserProfile: UserProfile? = null,
    val isAuthenticateButtonEnabled: Boolean = false,
    val implicitlyAuthenticatedUserProfile: UserProfile? = null,
  )

  sealed interface UiEvent {
    data object StartImplicitAuthentication : UiEvent
    data class UpdateSelectedUserProfile(val userProfile: UserProfile) : UiEvent
    data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
  }
}