package com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricAuthenticationViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when(event){
      is UiEvent.StartBiometricAuthentication -> TODO()
      is UiEvent.UpdateSelectedUserProfile -> TODO()
    }
  }

  private fun loadInitialData() {
    updateIsSdkInitialized()
    updateAuthenticatedProfile()
    viewModelScope.launch {
      updateUserProfiles()
      updateBiometricAuthenticatorRegistered()
      updateAuthenticateButton()
    }
  }

  private fun updateIsSdkInitialized() {
    isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
  }

  private fun updateAuthenticatedProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfile = it) }
      .onFailure { uiState = uiState.copy(authenticatedUserProfile = null) }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfiles = it, selectedUserProfile = it.firstOrNull()) }
      .onFailure { uiState = uiState.copy(userProfiles = emptySet()) }
  }

  private fun updateBiometricAuthenticatorRegistered() {
    uiState.selectedUserProfile?.let { userProfile ->
      getRegisteredAuthenticatorsUseCase.execute(userProfile)
        .map { authenticators -> authenticators.any { it.type == OneginiAuthenticator.Type.BIOMETRIC } }
        .onSuccess { uiState = uiState.copy(isBiometricAuthenticatorRegisteredForUser = it) }
        .onFailure { uiState = uiState.copy(isBiometricAuthenticatorRegisteredForUser = false) }
    } ?: { uiState = uiState.copy(isBiometricAuthenticatorRegisteredForUser = false) }
  }

  private fun updateAuthenticateButton() {
    uiState = uiState.copy(isAuthenticateButtonEnabled = uiState.isSdkInitialized && uiState.selectedUserProfile != null)
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfiles: Set<UserProfile> = emptySet(),
    val selectedUserProfile: UserProfile? = null,
    val authenticatedUserProfile: UserProfile? = null,
    val isBiometricAuthenticatorRegisteredForUser: Boolean = false,
    val isAuthenticateButtonEnabled: Boolean = false,
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null
  )

  sealed interface UiEvent {
    data class UpdateSelectedUserProfile(val userProfile: UserProfile) : UiEvent
    object StartBiometricAuthentication : UiEvent
  }
}
