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
import com.onewelcome.core.usecase.GetRegisteredAuthenticatorsUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.PinAuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinAuthenticationViewModel @Inject() constructor(
  isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getRegisteredAuthenticatorsUseCase: GetRegisteredAuthenticatorsUseCase,
  private val pinAuthenticationUseCase: PinAuthenticationUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
) : ViewModel() {
  var uiState by mutableStateOf(State())
    private set

  init {
    viewModelScope.launch {
      isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
      updateUserProfiles()
      updateCancellationButton()
    }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.StartPinAuthentication -> startPinAuthentication(event.userProfile)
      is UiEvent.CancelAuthentication -> cancelAuthentication()
    }
  }

  private fun updateCancellationButton() {
    uiState = uiState.copy(isAuthenticationCancellationEnabled = pinAuthenticationUseCase.isRegistrationInProgress())
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
      .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
  }

  private fun cancelAuthentication() {}

  private fun startPinAuthentication(userProfile: UserProfile) {
    viewModelScope.launch {
      getRegisteredAuthenticatorsUseCase.execute(userProfile)
        .onSuccess {
          val pinAuthenticator = it.first { it.type == OneginiAuthenticator.Type.PIN }
          pinAuthenticationUseCase.execute(userProfile, pinAuthenticator)
            .onSuccess { uiState = uiState.copy(result = Ok(it)) }
            .onFailure { uiState = uiState.copy(result = Err(it)) }
        }
        .onFailure { uiState = uiState.copy(result = Err(it)) }
    }
  }

  data class State(
    val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    val isSdkInitialized: Boolean = false,
    val userProfileIds: List<String> = emptyList(),
    val isAuthenticationCancellationEnabled: Boolean = false,
  )

  sealed interface UiEvent {
    data class StartPinAuthentication(val userProfile: UserProfile) : UiEvent
    data object CancelAuthentication : UiEvent
  }
}
