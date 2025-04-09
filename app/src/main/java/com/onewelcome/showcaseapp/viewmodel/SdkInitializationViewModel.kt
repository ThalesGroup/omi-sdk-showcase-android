package com.onewelcome.showcaseapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onewelcome.showcaseapp.usecase.OmiSdkInitializationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SdkInitializationViewModel @Inject constructor(
  private val omiSdkInitializationUseCase: OmiSdkInitializationUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.ChangeHttpConnectTimeoutValue -> uiState = uiState.copy(httpConnectTimeout = event.value)
      is UiEvent.ChangeHttpReadTimeoutValue -> uiState = uiState.copy(httpReadTimeout = event.value)
      is UiEvent.ChangeShouldStoreCookiesValue -> uiState = uiState.copy(shouldStoreCookies = event.value)
      is UiEvent.InitializeOneginiSdk -> omiSdkInitializationUseCase.initialize()
    }
  }

  data class State(
    val shouldStoreCookies: Boolean = true,
    val httpConnectTimeout: Int? = null,
    val httpReadTimeout: Int? = null
  )

  sealed interface UiEvent {
    data class ChangeShouldStoreCookiesValue(val value: Boolean) : UiEvent
    data class ChangeHttpConnectTimeoutValue(val value: Int?) : UiEvent
    data class ChangeHttpReadTimeoutValue(val value: Int?) : UiEvent
    data object InitializeOneginiSdk : UiEvent
  }
}
