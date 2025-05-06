package com.onewelcome.showcaseapp.feature.info

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
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateStatus() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
    viewModelScope.launch {
      getUserProfilesUseCase.execute()
        .onSuccess { uiState = uiState.copy(userProfiles = Ok(it.toList())) }
        .onFailure { uiState = uiState.copy(userProfiles = Err(it)) }
    }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfiles: Result<List<UserProfile>, Throwable>? = null
  )
}
