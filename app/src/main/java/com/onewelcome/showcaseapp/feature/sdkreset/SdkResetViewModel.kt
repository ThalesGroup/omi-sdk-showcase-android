package com.onewelcome.showcaseapp.feature.sdkreset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.SdkResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SdkResetViewModel @Inject constructor(
  private val sdkResetUseCase: SdkResetUseCase,
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  init {
    loadInitialData()
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.ResetSdk -> resetSdk()
    }
  }

  private fun loadInitialData() {
    uiState = uiState.copy(
      isSdkInitialized = isSdkInitializedUseCase.execute(),
      authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
    )
  }

  private fun resetSdk() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      uiState = uiState.copy(
        result = sdkResetUseCase.execute(),
        isLoading = false,
        isSdkInitialized = isSdkInitializedUseCase.execute(),
        authenticatedUserProfile = getAuthenticatedUserProfileUseCase.execute().get()
      )
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val result: Result<Unit, Throwable>? = null,
    val authenticatedUserProfile: UserProfile? = null
  )

  sealed interface UiEvent {
    data object ResetSdk : UiEvent
  }
}
