package com.onewelcome.showcaseapp.feature.singlesignon

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.OneginiAppToWebSingleSignOn
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.SingleSignOnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleSignOnViewModel @Inject constructor(
  private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
  private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
  private val singleSignOnUseCase: SingleSignOnUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  fun updateData() {
    updateIsSdkInitialized()
    updateAuthenticatedUserProfile()
  }

  fun onEvent(event: Event) {
    when (event) {
      is Event.OpenUrl -> {
        viewModelScope.launch {
          uiState = uiState.copy(result = singleSignOnUseCase.execute(event.url))
        }
      }
    }
  }

  private fun updateIsSdkInitialized() {
    uiState = uiState.copy(isSdkInitialized = isSdkInitializedUseCase.execute())
  }

  private fun updateAuthenticatedUserProfile() {
    getAuthenticatedUserProfileUseCase.execute()
      .onSuccess { uiState = uiState.copy(userProfile = it) }
      .onFailure { uiState = uiState.copy(userProfile = null) }
  }

  data class State(
    val isSdkInitialized: Boolean = false,
    val userProfile: UserProfile? = null,
    val result: Result<OneginiAppToWebSingleSignOn, Throwable>? = null,
  )

  sealed class Event {
    data class OpenUrl(val url: Uri) : Event()
  }
}
