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
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.BrowserIdentityProvider
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserRegistrationViewModel @Inject constructor(
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val browserRegistrationUseCase: BrowserRegistrationUseCase,
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  //We could also use init {} in InfoViewModel instead of fun updateStatus()
  init {
    viewModelScope.launch {
      isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
      browserRegistrationUseCase.getBrowserIdentityProviders()
        .onSuccess {
          val updatedIdentityProviders = addDefaultIdentityProvider(it)
          uiState = uiState.copy(identityProviders = updatedIdentityProviders)
        }
        .onFailure { uiState = uiState.copy(identityProviders = emptyList()) }
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartBrowserRegistration -> register()
      is UiEvent.UpdateSelectedIdentityProvider -> uiState = uiState.copy(selectedIdentityProvider = event.identityProvider)
      is UiEvent.UpdateSelectedScopes -> uiState = uiState.copy(selectedScopes = event.scopes)
      is UiEvent.CancelRegistration -> cancelRegistration()
    }
  }

  private fun addDefaultIdentityProvider(identityProviders: List<BrowserIdentityProvider>): List<BrowserIdentityProvider> {
    return listOf(BrowserIdentityProvider.DEFAULT_IDENTITY_PROVIDER) + identityProviders
  }

  private fun cancelRegistration() {
    viewModelScope.launch {
      browserRegistrationUseCase.cancelRegistration()
      uiState = uiState.copy(isLoading = false)
    }
  }

  private fun register() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true)
      browserRegistrationUseCase
        .register(identityProvider = mapIdentityProvider(), scopes = uiState.selectedScopes)
        .onSuccess { uiState = uiState.copy(result = Ok(it), isLoading = false) }
        .onFailure { uiState = uiState.copy(result = Err(it), isLoading = false) }
    }
  }

  private fun mapIdentityProvider(): BrowserIdentityProvider? =
    if (uiState.selectedIdentityProvider == BrowserIdentityProvider.DEFAULT_IDENTITY_PROVIDER) null else uiState.selectedIdentityProvider

  data class State(
    val isLoading: Boolean = false,
    //TODO: Przegadaj Throwable z Alkiem. Gubimy numer errora.
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    val identityProviders: List<BrowserIdentityProvider> = emptyList(),
    val isSdkInitialized: Boolean = false,
    val selectedIdentityProvider: BrowserIdentityProvider? = null,
    val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
  )

  sealed interface UiEvent {
    data object StartBrowserRegistration : UiEvent
    data object CancelRegistration : UiEvent
    data class UpdateSelectedIdentityProvider(val identityProvider: BrowserIdentityProvider) : UiEvent
    data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
  }
}
