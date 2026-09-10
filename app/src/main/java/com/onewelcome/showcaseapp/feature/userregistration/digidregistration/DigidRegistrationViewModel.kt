// POC - START
package com.onewelcome.showcaseapp.feature.userregistration.digidregistration

import android.util.Log
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
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.OmiSdkEngine
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.DigiDRegistrationRequestHandler
import com.onewelcome.core.usecase.StatelessUserRegistrationUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DigidRegistrationViewModel @Inject constructor(
    private val omiSdkEngine: OmiSdkEngine,
    private val userRegistrationUseCase: UserRegistrationUseCase,
    private val statelessUserRegistrationUseCase: StatelessUserRegistrationUseCase,
    private val digiDRegistrationRequestHandler: DigiDRegistrationRequestHandler,
  private val createPinRequestHandler: CreatePinRequestHandler
) : ViewModel() {


    var uiState by mutableStateOf(State())
        private set

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        updateIsSdkInitialized()
        updateUserProfiles()
        updateIdentityProviders()

      viewModelScope.launch {
        digiDRegistrationRequestHandler.authenticationUrlFlow.collect { stepData ->
          _navigationEvents.emit(NavigationEvent.OpenDigidUrl(stepData.authenticationUrl))
        }

      }
      viewModelScope.launch {
        createPinRequestHandler.startPinCreationFlow.collect {
          _navigationEvents.emit(NavigationEvent.ToPinScreen)
        }
      }
    }

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.EnterOptionalData -> register()
            is UiEvent.SetStatelessRegistration -> setStatelessRegistration(event.isStatelessRegistration)
            is UiEvent.UpdateSelectedIdentityProvider -> updateSelectedIdentityProvider(event.identityProvider)
            is UiEvent.UpdateSelectedScopes -> updateSelectedScopes(event.scopes)
            is UiEvent.CancelRegistration -> cancelRegistration()
        }
    }

    private fun register() {
        uiState = uiState.copy(result = null)
        uiState = uiState.copy(isRegistrationCancellationEnabled = true)

        if (uiState.isStatelessRegistration) {
            registerStatelessUser()
        } else {
            registerUser()
        }
    }

    private fun cancelRegistration() {
        uiState = uiState.copy(isRegistrationCancellationEnabled = false)

    }

    private fun setStatelessRegistration(isStatelessRegistration: Boolean) {
        uiState = uiState.copy(isStatelessRegistration = isStatelessRegistration)
    }

    private fun updateSelectedIdentityProvider(identityProvider: OneginiIdentityProvider) {
        uiState = uiState.copy(selectedIdentityProvider = identityProvider)
    }

    private fun updateSelectedScopes(scopes: List<String>) {
        uiState = uiState.copy(selectedScopes = scopes)
    }

    private fun updateIsSdkInitialized() {
        uiState = uiState.copy(isSdkInitialized = true)
    }

    private fun updateUserProfiles() {
        omiSdkEngine.oneginiClient.getUserClient()?.userProfiles?.let { profiles ->
            uiState = uiState.copy(userProfileIds = profiles.map { it.profileId })
        }
    }

  var identityProvider: OneginiIdentityProvider? = null
  private fun updateIdentityProviders() {
    omiSdkEngine.oneginiClient.getUserClient()?.identityProviders?.let { providers ->
      // Filter to only include 'poc_digid_two_step'
      identityProvider = providers.filter { it.id == "poc_digid_two_step" }.firstOrNull()
      val digidProviders = providers.toSet()
      uiState = uiState.copy(identityProviders = digidProviders)
    }
  }

  private suspend fun handleSuccess(pair: Pair<UserProfile, CustomInfo?>) {
    uiState = uiState.copy(result = Ok(pair), isRegistrationCancellationEnabled = false)
    updateUserProfiles()
  }

  private fun handleFailure(throwable: Throwable) {
    val isActionAlreadyInProgressError =
      throwable is OneginiRegistrationError && throwable.errorType == OneginiRegistrationError.Type.ACTION_ALREADY_IN_PROGRESS
    uiState = if (isActionAlreadyInProgressError) {
      uiState.copy(result = Err(throwable))
    } else {
      uiState.copy(result = Err(throwable), isRegistrationCancellationEnabled = false)
    }
  }
  private fun registerUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isRegistrationCancellationEnabled = true)
      userRegistrationUseCase
        .register(identityProvider = identityProvider, scopes = uiState.selectedScopes)
        .onSuccess { handleSuccess(it) }
        .onFailure { handleFailure(it) }
    }
  }

  private fun registerStatelessUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isRegistrationCancellationEnabled = true)
      statelessUserRegistrationUseCase
        .execute(identityProvider = identityProvider, scopes = uiState.selectedScopes)
        .onSuccess { handleSuccess(UserProfile.stateless to it) }
        .onFailure { handleFailure(it) }
    }
  }

    data class State(
        val isSdkInitialized: Boolean = false,
        val identityProviders: Set<OneginiIdentityProvider> = emptySet(),
        val selectedIdentityProvider: OneginiIdentityProvider? = identityProviders.firstOrNull(),
        val userProfileIds: List<String> = emptyList(),
        val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
        val isStatelessRegistration: Boolean = false,
        val isRegistrationCancellationEnabled: Boolean = false,
        val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null
    )

    sealed interface NavigationEvent {
        object ToPinScreen : NavigationEvent
        object ToTwoStepInputScreen : NavigationEvent
        object ToDigidRegistrationScreen : NavigationEvent
        object ToTwoStepVerificationScreen : NavigationEvent
      data class  OpenDigidUrl(val url : String): NavigationEvent
    }

    sealed interface UiEvent {
        object EnterOptionalData : UiEvent
        object CancelRegistration : UiEvent
        data class SetStatelessRegistration(val isStatelessRegistration: Boolean) : UiEvent
        data class UpdateSelectedIdentityProvider(val identityProvider: OneginiIdentityProvider) : UiEvent
        data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
    }
}
// POC - END
