package com.onewelcome.showcaseapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onewelcome.showcaseapp.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateStatus() {
    uiState = uiState.copy(
      isSdkInitialized = isSdkInitializedUseCase.execute()
    )
  }

  data class State(
    val isSdkInitialized: Boolean = false
  )
}
