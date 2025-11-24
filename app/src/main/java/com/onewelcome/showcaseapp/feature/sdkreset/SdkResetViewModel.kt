package com.onewelcome.showcaseapp.feature.sdkreset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.SdkResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SdkResetViewModel @Inject constructor(
  private val sdkResetUseCase: SdkResetUseCase,
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
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
    val isSdkInitialized = isSdkInitializedUseCase.execute()
    uiState = uiState.copy(isSdkInitialized = isSdkInitialized)
  }

  private fun resetSdk() {
    uiState = uiState.copy(isLoading = true)
    viewModelScope.launch {
      val resetResult = sdkResetUseCase.execute()
      uiState = uiState.copy(
        result = resetResult,
        isLoading = false,
        isSdkInitialized = isSdkInitializedUseCase.execute()
      )
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val result: Result<Unit, Throwable>? = null
  )

  sealed interface UiEvent {
    data object ResetSdk : UiEvent
  }
}
