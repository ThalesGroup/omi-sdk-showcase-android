package com.onewelcome.showcaseapp.feature.userregistration.browserregistration

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
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.PinUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserRegistrationViewModel @Inject constructor(
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val browserRegistrationUseCase: BrowserRegistrationUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val pinUseCase: PinUseCase
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
      updateIdentityProviders()
      updateSelectedIdentityProvider()
      updateUserProfiles()
      updateCancellationButton()
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartBrowserRegistration -> startRegistration()
      is UiEvent.UpdateSelectedIdentityProvider -> uiState = uiState.copy(selectedIdentityProvider = event.identityProvider)
      is UiEvent.UpdateSelectedScopes -> uiState = uiState.copy(selectedScopes = event.scopes)
      is UiEvent.CancelRegistration -> cancelRegistration()
      is UiEvent.UseDefaultIdentityProvider -> uiState = uiState.copy(shouldUseDefaultIdentityProvider = event.isChecked)
    }
  }

  private suspend fun updateIdentityProviders() {
    browserRegistrationUseCase.getBrowserIdentityProviders()
      .onSuccess { uiState = uiState.copy(identityProviders = it) }
      .onFailure { uiState = uiState.copy(identityProviders = emptySet()) }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun updateSelectedIdentityProvider() {
    val identityProviders = uiState.identityProviders
    if (identityProviders.isNotEmpty()) {
      uiState = uiState.copy(selectedIdentityProvider = identityProviders.first())
    }
  }

  private fun updateCancellationButton() {
    uiState = uiState.copy(isRegistrationCancellationEnabled = browserRegistrationUseCase.isRegistrationInProgress())
  }

  private fun cancelRegistration() {
    viewModelScope.launch {
      browserRegistrationUseCase.cancelRegistration()
    }
  }

  private fun startRegistration() {
    registerUser()
    listenForPinScreenNavigationEvent()
  }

  private fun listenForPinScreenNavigationEvent() {
    viewModelScope.launch {
      pinUseCase.pinCreationEventFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinScreen)
      }
    }
  }

  private fun registerUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isRegistrationCancellationEnabled = true)
      browserRegistrationUseCase
        .register(identityProvider = getIdentityProvider(), scopes = uiState.selectedScopes)
        .onSuccess {
          uiState = uiState.copy(result = Ok(it), isRegistrationCancellationEnabled = false)
          updateUserProfiles()
        }
        .onFailure { uiState = uiState.copy(result = Err(it), isRegistrationCancellationEnabled = false) }
    }
  }

  private fun getIdentityProvider(): OneginiIdentityProvider? =
    if (uiState.shouldUseDefaultIdentityProvider) null else uiState.selectedIdentityProvider

  data class State(
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    val identityProviders: Set<OneginiIdentityProvider> = emptySet(),
    val isSdkInitialized: Boolean = false,
    val selectedIdentityProvider: OneginiIdentityProvider? = null,
    val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
    val shouldUseDefaultIdentityProvider: Boolean = false,
    val userProfileIds: List<String> = emptyList(),
    val isRegistrationCancellationEnabled: Boolean = false,
    val shouldNavigateToPinScreen: Boolean = false,
  )

  sealed interface UiEvent {
    data object StartBrowserRegistration : UiEvent
    data object CancelRegistration : UiEvent
    data class UpdateSelectedIdentityProvider(val identityProvider: OneginiIdentityProvider) : UiEvent
    data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
    data class UseDefaultIdentityProvider(val isChecked: Boolean) : UiEvent
  }

  sealed class NavigationEvent {
    object ToPinScreen : NavigationEvent()
  }
}
