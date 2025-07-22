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
import com.onewelcome.core.usecase.GetAuthenticatedUserProfilesUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getUserProfilesUseCase: GetUserProfilesUseCase,
  private val getAuthenticatedUserProfilesUseCase: GetAuthenticatedUserProfilesUseCase,
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateData() {
    updateIsSdkInitialized()
    viewModelScope.launch {
      updateUserProfiles()
      updateAuthenticatedUserProfiles()
    }
  }

  private suspend fun updateUserProfiles() {
    getUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfileIds = Ok(it.map { it.profileId }.toList())) }
      .onFailure { uiState = uiState.copy(userProfileIds = Err(Unit)) }
  }

  private suspend fun updateAuthenticatedUserProfiles() {
    getAuthenticatedUserProfilesUseCase.execute()
      .onSuccess { uiState = uiState.copy(authenticatedUserProfileId = Ok(it?.profileId)) }
      .onFailure { uiState = uiState.copy(authenticatedUserProfileId = Err(Unit)) }
  }

  private fun updateIsSdkInitialized() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfileIds: Result<List<String>, Unit>? = null,
    val authenticatedUserProfileId: Result<String?, Unit>? = null,
  )
}
