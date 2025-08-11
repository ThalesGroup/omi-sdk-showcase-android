package com.onewelcome.showcaseapp.feature.logout

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
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.LogoutUser -> logoutUser()
    }
  }

  private fun loadInitialData() {
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    uiState = uiState.copy(isSdkInitialized = isSdkInitialized)
    if (isSdkInitialized) {
      updateAuthenticatedUserProfile()
    }
  }

  private fun updateAuthenticatedUserProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfile = it) }
      .onFailure { uiState = uiState.copy(authenticatedUserProfile = null) }
  }

  private fun logoutUser() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      logoutUseCase.execute()
        .onSuccess {
          uiState = uiState.copy(
            result = Ok(it),
            authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().value,
            isLoading = false,
          )
        }
        .onFailure { uiState = uiState.copy(result = Err(it), isLoading = false) }
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val authenticatedUserProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val result: Result<Unit, Throwable>? = null
  )

  sealed interface UiEvent {
    data object LogoutUser : UiEvent
  }
}
