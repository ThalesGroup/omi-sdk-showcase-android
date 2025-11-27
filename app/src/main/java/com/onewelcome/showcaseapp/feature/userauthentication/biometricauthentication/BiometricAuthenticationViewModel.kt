package com.onewelcome.showcaseapp.feature.userauthentication.biometricauthentication

import androidx.biometric.BiometricPrompt
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
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.BiometricAuthenticationHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.BiometricAuthenticationUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricAuthenticationViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val biometricAuthenticationUseCase: BiometricAuthenticationUseCase,
  private val biometricAuthenticationHandler: BiometricAuthenticationHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    loadInitialData()
    listenForBiometricPromptEvent()
    listenForPinScreenNavigationEvent()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.UpdateSelectedUserProfile -> uiState = uiState.copy(selectedUserProfile = event.userProfile)
      is UiEvent.StartBiometricAuthentication -> authenticateUser()
      is UiEvent.BiometricAuthenticationError -> biometricAuthenticationHandler.biometricCallback?.onBiometricAuthenticationError(event.errorCode)
      is UiEvent.BiometricAuthenticationSuccess -> biometricAuthenticationHandler.biometricCallback?.userAuthenticatedSuccessfully()
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

  private fun listenForBiometricPromptEvent() {
    viewModelScope.launch {
      biometricAuthenticationHandler.startBiometricAuthenticationFlow.collect { cryptoObject ->
        _navigationEvents.send(NavigationEvent.ShowBiometricPrompt(cryptoObject))
      }
    }
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
        biometricAuthenticationUseCase.execute(selectedUserProfile)
          .onSuccess { uiState = uiState.copy(authenticatedUserProfile = it.first) }
          .also { result -> uiState = uiState.copy(result = result, isLoading = false) }
      }
    } ?: run {
      uiState = uiState.copy(result = Err(IllegalArgumentException("User profile not selected")))
    }
  }

  data class State(
    val isLoading: Boolean = false,
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
    data object BiometricAuthenticationSuccess : UiEvent
    data class BiometricAuthenticationError(val errorCode: Int) : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
    data class ShowBiometricPrompt(val cryptoObject: BiometricPrompt.CryptoObject) : NavigationEvent
  }
}
