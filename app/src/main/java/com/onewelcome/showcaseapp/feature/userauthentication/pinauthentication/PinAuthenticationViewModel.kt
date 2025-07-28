package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapCatching
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.PinAuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinAuthenticationViewModel @Inject() constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val pinAuthenticationUseCase: PinAuthenticationUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartPinAuthentication -> startPinAuthentication()
      is UiEvent.CancelAuthentication -> cancelAuthentication()
      is UiEvent.UpdateSelectedUserProfile -> updateSelectedUserProfile(event)
      is UiEvent.LoadData -> loadData()
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
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfile = it) }
      .onFailure { uiState = uiState.copy(authenticatedUserProfile = null) }
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

  private fun cancelAuthentication() {
    pinAuthenticationRequestHandler.pinCallback?.denyAuthenticationRequest()
  }

  private fun startPinAuthentication() {
    authenticateUser()
    listenForPinScreenNavigationEvent()
  }

  private fun authenticateUser() {
    uiState.selectedUserProfile?.let { selectedUserProfile ->
      viewModelScope.launch {
        getRegisteredAuthenticatorsUseCase.execute(selectedUserProfile)
          .mapCatching { it.first { it.type == OneginiAuthenticator.Type.PIN } }
          .flatMap { pinAuthenticationUseCase.execute(selectedUserProfile, it) }
          .onSuccess { uiState = uiState.copy(result = Ok(it)) }
          .onFailure { uiState = uiState.copy(result = Err(it)) }
      }
    } ?: run {
      uiState = uiState.copy(result = Err(IllegalArgumentException("User profile not selected")))
    }
  }

  private fun listenForPinScreenNavigationEvent() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.startPinAuthenticationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinScreen)
      }
    }
  }

  data class State(
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val userProfiles: Set<UserProfile> = emptySet(),
    val selectedUserProfile: UserProfile? = null,
    val isAuthenticateButtonEnabled: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
  )

  sealed interface UiEvent {
    data object StartPinAuthentication : UiEvent
    data object CancelAuthentication : UiEvent
    data class UpdateSelectedUserProfile(val userProfile: UserProfile) : UiEvent
    data object LoadData : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
  }
}
