package com.onewelcome.showcaseapp.feature.userauthentication.authenticators

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import com.onewelcome.core.usecase.DeregisterAuthenticatorUseCase
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetAuthenticatorsUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.RegisterAuthenticatorUseCase
import com.onewelcome.showcaseapp.feature.userauthentication.authenticators.AuthenticatorSettingsViewModel.UiEvent.ToggleAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticatorSettingsViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val getAuthenticatorsUseCase: GetAuthenticatorsUseCase,
  private val registerAuthenticatorUseCase: RegisterAuthenticatorUseCase,
  private val deregisterAuthenticatorUseCase: DeregisterAuthenticatorUseCase,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    loadStatus()
    listenForPinInputScreenNavigationEvents()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is ToggleAuthenticator -> toggleAuthenticator(event.authenticator)
    }
  }

  private fun loadStatus() {
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    val authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
    val availableAuthenticators = authenticatedUserProfile?.let { getAuthenticatorsUseCase.execute(it).get() } ?: emptySet()
    uiState = uiState.copy(
      isSdkInitialized = isSdkInitialized,
      authenticatedUserProfile = authenticatedUserProfile,
      availableAuthenticators = availableAuthenticators
    )
  }

  private fun toggleAuthenticator(authenticator: OneginiAuthenticator) {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true)
      val result = if (authenticator.isRegistered) {
        deregisterAuthenticator(authenticator)
      } else {
        registerAuthenticator(authenticator)
      }
      loadStatus()
      uiState = uiState.copy(result = result, isLoading = false)
    }
  }

  private suspend fun registerAuthenticator(authenticator: OneginiAuthenticator): AuthenticatorOperationResult {
    return registerAuthenticatorUseCase.execute(authenticator).fold(
      success = { AuthenticatorOperationResult.RegisterSuccess(it) },
      failure = { AuthenticatorOperationResult.Error(it) }
    )
  }

  private suspend fun deregisterAuthenticator(authenticator: OneginiAuthenticator): AuthenticatorOperationResult {
    return deregisterAuthenticatorUseCase.execute(authenticator).fold(
      success = { AuthenticatorOperationResult.DeregisterSuccess },
      failure = { AuthenticatorOperationResult.Error(it) }
    )
  }

  private fun listenForPinInputScreenNavigationEvents() {
    viewModelScope.launch {
      pinAuthenticationRequestHandler.startPinAuthenticationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinAuthenticationScreen)
      }
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val availableAuthenticators: Set<OneginiAuthenticator> = emptySet(),
    val isLoading: Boolean = false,
    val result: AuthenticatorOperationResult? = null
  )

  sealed interface AuthenticatorOperationResult {
    data class RegisterSuccess(val customInfo: CustomInfo?) : AuthenticatorOperationResult
    data object DeregisterSuccess : AuthenticatorOperationResult
    data class Error(val throwable: Throwable) : AuthenticatorOperationResult
  }

  sealed interface UiEvent {
    data class ToggleAuthenticator(val authenticator: OneginiAuthenticator) : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinAuthenticationScreen : NavigationEvent
  }
}
