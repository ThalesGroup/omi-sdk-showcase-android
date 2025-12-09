package com.onewelcome.showcaseapp.feature.userregistration.onestepregistration

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
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.StatelessUserRegistrationUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OneStepRegistrationViewModel @Inject constructor(
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val userRegistrationUseCase: UserRegistrationUseCase,
  private val statelessUserRegistrationUseCase: StatelessUserRegistrationUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val createPinRequestHandler: CreatePinRequestHandler,
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
      is UiEvent.StartOneStepRegistration -> startRegistration()
      is UiEvent.CancelRegistration -> cancelRegistration()
      is UiEvent.SetStatelessRegistration -> uiState = uiState.copy(isStatelessRegistration = event.isStateless)
      is UiEvent.UpdateSelectedScopes -> uiState = uiState.copy(selectedScopes = event.scopes)
      is UiEvent.UpdateOtpValue -> uiState = uiState.copy(otp = event.otp)
    }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun updateCancellationButton() {
    val isRegistrationInProgress = createPinRequestHandler.isPinCreationInProgress()
    uiState = uiState.copy(isRegistrationCancellationEnabled = isRegistrationInProgress)
  }

  private fun cancelRegistration() {
    viewModelScope.launch {
      createPinRequestHandler.cancelPinCreation()
    }
  }

  private fun startRegistration() {
    if (uiState.isStatelessRegistration) {
      registerStatelessUser()
    } else {
      registerUser()
      listenForPinScreenNavigationEvent()
    }
  }

  private fun listenForPinScreenNavigationEvent() {
    viewModelScope.launch {
      createPinRequestHandler.startPinCreationFlow.collect {
        _navigationEvents.send(NavigationEvent.ToPinScreen)
      }
    }
  }

  private fun registerStatelessUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isRegistrationCancellationEnabled = true)
      statelessUserRegistrationUseCase
        .execute(identityProvider = null, scopes = uiState.selectedScopes)
        .onSuccess { handleSuccess(UserProfile.stateless to it) }
        .onFailure { handleFailure(it) }
    }
  }

  private fun registerUser() {
    viewModelScope.launch {
      uiState = uiState.copy(isRegistrationCancellationEnabled = true)
      userRegistrationUseCase
        .register(identityProvider = null, scopes = uiState.selectedScopes)
        .onSuccess { handleSuccess(it) }
        .onFailure { handleFailure(it) }
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

  data class State(
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
    val userProfileIds: List<String> = emptyList(),
    val isRegistrationCancellationEnabled: Boolean = false,
    val isStatelessRegistration: Boolean = false,
    val otp: String = ""
  )

  sealed interface UiEvent {
    data object StartOneStepRegistration : UiEvent
    data object CancelRegistration : UiEvent
    data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
    data class SetStatelessRegistration(val isStateless: Boolean) : UiEvent
    data class UpdateOtpValue(val otp: String) : UiEvent
  }

  sealed interface NavigationEvent {
    data object ToPinScreen : NavigationEvent
  }
}
