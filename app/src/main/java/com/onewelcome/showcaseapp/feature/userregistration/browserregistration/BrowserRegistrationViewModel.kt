package com.onewelcome.showcaseapp.feature.userregistration.browserregistration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onewelcome.core.omisdk.entity.IdentityProvider.BrowserIdentityProvider
import com.onewelcome.core.usecase.BrowserRegistrationUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
    browserRegistrationUseCase.browserIdentityProviders
      .onSuccess { uiState = uiState.copy(identityProviders = it) }
      .onFailure { uiState = uiState.copy(identityProviders = emptyList()) }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartBrowserRegistration -> register()
    }
  }

  private fun register() {
    uiState = uiState.copy(isLoading = true)
    browserRegistrationUseCase.register()
    viewModelScope.launch {
      delay(1000)
      uiState = uiState.copy(isLoading = false)
    }
  }

  data class State(
    val isLoading: Boolean = false,
    val result: Result<Unit, OneginiRegistrationError>? = null,
    val identityProviders: List<BrowserIdentityProvider> = emptyList(),
    val isSdkInitialized: Boolean = false
  )

  sealed interface UiEvent {
    data object StartBrowserRegistration : UiEvent
  }
}