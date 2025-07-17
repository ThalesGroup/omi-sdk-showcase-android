package com.onewelcome.showcaseapp.feature.userauthentication.pinauthentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
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
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val pinAuthenticationUseCase: PinAuthenticationUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
      updateUserProfiles()
      updateCancellationButton()
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartPinAuthentication -> startPinAuthentication()
      is UiEvent.CancelAuthentication -> cancelAuthentication()
    }
  }

  private fun updateCancellationButton() {
//    uiState = uiState.copy(isAuthenticationCancellationEnabled = pinAuthenticationUseCase.isAuthenticationInProgress())
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun cancelAuthentication() {}

  private fun startPinAuthentication() {
    authenticateUser()
    listenForPinScreenNavigationEvent()
  }

  private fun authenticateUser() {
    uiState.selectedUserProfile?.let { selectedUserProfile ->
      viewModelScope.launch {
        getRegisteredAuthenticatorsUseCase.execute(selectedUserProfile)
          .onSuccess {
            val pinAuthenticator = it.first { it.type == OneginiAuthenticator.Type.PIN }
            pinAuthenticationUseCase.execute(selectedUserProfile, pinAuthenticator)
              .onSuccess { uiState = uiState.copy(result = Ok(it)) }
              .onFailure { uiState = uiState.copy(result = Err(it)) }
          }
          .onFailure { uiState = uiState.copy(result = Err(it)) }
      }
    } ?: {
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
    val userProfileIds: List<String> = emptyList(),
    val selectedUserProfile: UserProfile? = null,
    val isAuthenticationCancellationEnabled: Boolean = false,
  )

  sealed interface UiEvent {
    data object StartPinAuthentication : UiEvent
    data object CancelAuthentication : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
  }
}
