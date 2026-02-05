package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.CustomAuthenticator
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.CustomAuthenticationUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsCustomAuthHandlerRegisteredUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomAuthenticationViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val customAuthenticationUseCase: CustomAuthenticationUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val isCustomAuthHandlerRegisteredUseCase: IsCustomAuthHandlerRegisteredUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    loadInitialData()
    listenForPinScreenNavigationEvent()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.UpdateSelectedUserProfile -> {
        uiState = uiState.copy(selectedUserProfile = event.userProfile)
        updateCustomAuthenticatorRegistered()
        updateAuthenticateButton()
      }

      is UiEvent.StartCustomAuthentication -> authenticateUser()
      is UiEvent.NavigateToSdkInitialization -> {
        viewModelScope.launch {
          _navigationEvents.send(NavigationEvent.ToSdkInitialization)
        }
      }
    }
  }

  private fun loadInitialData() {
    updateIsSdkInitialized()
    updateIsCustomAuthHandlerRegistered()
    updateAuthenticatedProfile()
    viewModelScope.launch {
      updateUserProfiles()
      updateCustomAuthenticatorRegistered()
      updateAuthenticateButton()
    }
  }

  private fun updateIsSdkInitialized() {
    isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
  }

  private fun updateIsCustomAuthHandlerRegistered() {
    val isRegistered = isCustomAuthHandlerRegisteredUseCase.execute()
    uiState = uiState.copy(isCustomAuthHandlerRegistered = isRegistered)
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

  private fun updateCustomAuthenticatorRegistered() {
    uiState.selectedUserProfile?.let { userProfile ->
      getRegisteredAuthenticatorsUseCase.execute(userProfile)
        .map { authenticators -> authenticators.any { it.id == CustomAuthenticator.CUSTOM_AUTHENTICATOR_ID } }
        .onSuccess { uiState = uiState.copy(isCustomAuthenticatorRegisteredForUser = it) }
        .onFailure { uiState = uiState.copy(isCustomAuthenticatorRegisteredForUser = false) }
    } ?: run { uiState = uiState.copy(isCustomAuthenticatorRegisteredForUser = false) }
  }

  private fun updateAuthenticateButton() {
    uiState = uiState.copy(
      isAuthenticateButtonEnabled = uiState.isSdkInitialized
          && uiState.selectedUserProfile != null
          && uiState.isCustomAuthenticatorRegisteredForUser
          && uiState.isCustomAuthHandlerRegistered
    )
  }

  private fun listenForPinScreenNavigationEvent() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.startPinAuthenticationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinScreen)
      }
    }
  }

  private fun authenticateUser() {
    uiState.selectedUserProfile?.let { selectedUserProfile ->
      viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        customAuthenticationUseCase.execute(selectedUserProfile)
          .onSuccess {
            uiState = uiState.copy(authenticatedUserProfile = selectedUserProfile)
            updateAuthenticatedProfile()
          }
          .also { result -> uiState = uiState.copy(result = result, isLoading = false) }
      }
    } ?: run {
      uiState = uiState.copy(result = Err(IllegalArgumentException("User profile not selected")))
    }
  }

  data class State(
    val isLoading: Boolean = false,
    val isSdkInitialized: Boolean = false,
    val isCustomAuthHandlerRegistered: Boolean = false,
    val userProfiles: Set<UserProfile> = emptySet(),
    val selectedUserProfile: UserProfile? = null,
    val authenticatedUserProfile: UserProfile? = null,
    val isCustomAuthenticatorRegisteredForUser: Boolean = false,
    val isAuthenticateButtonEnabled: Boolean = false,
    val result: Result<CustomInfo?, Throwable>? = null
  )

  sealed interface UiEvent {
    data class UpdateSelectedUserProfile(val userProfile: UserProfile) : UiEvent
    data object StartCustomAuthentication : UiEvent
    data object NavigateToSdkInitialization : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
    data object ToSdkInitialization : NavigationEvent
  }
}
