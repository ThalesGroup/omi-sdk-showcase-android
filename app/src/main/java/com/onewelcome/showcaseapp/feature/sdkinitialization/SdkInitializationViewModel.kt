package com.onewelcome.showcaseapp.feature.sdkinitialization

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
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.usecase.OmiSdkInitializationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
      is UiEvent.ChangeDeviceConfigCacheDurationValue -> uiState = uiState.copy(deviceConfigCacheDurationSeconds = event.value)
      is UiEvent.InitializeOneginiSdk -> initializeOmiSdk()
    }
  }

  private fun initializeOmiSdk() {
    val settings = OmiSdkInitializationSettings(
      shouldStoreCookies = uiState.shouldStoreCookies,
      httpConnectTimeout = uiState.httpConnectTimeout,
      httpReadTimeout = uiState.httpReadTimeout,
      deviceConfigCacheDuration = uiState.deviceConfigCacheDurationSeconds
    )
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true)
      omiSdkInitializationUseCase.initialize(settings)
        .onSuccess { uiState = uiState.copy(result = Ok(it)) }
        .onFailure { uiState = uiState.copy(result = Err(it)) }
        .also { uiState = uiState.copy(isLoading = false) }
    }
  }

  data class State(
    val shouldStoreCookies: Boolean = true,
    val httpConnectTimeout: Int? = null,
    val httpReadTimeout: Int? = null,
    val deviceConfigCacheDurationSeconds: Int? = null,
    val isLoading: Boolean = false,
    val result: Result<Set<UserProfile>, OneginiInitializationError>? = null
  )

  sealed interface UiEvent {
    data class ChangeShouldStoreCookiesValue(val value: Boolean) : UiEvent
    data class ChangeHttpConnectTimeoutValue(val value: Int?) : UiEvent
    data class ChangeHttpReadTimeoutValue(val value: Int?) : UiEvent
    data class ChangeDeviceConfigCacheDurationValue(val value: Int?) : UiEvent
    data object InitializeOneginiSdk : UiEvent
  }
}
