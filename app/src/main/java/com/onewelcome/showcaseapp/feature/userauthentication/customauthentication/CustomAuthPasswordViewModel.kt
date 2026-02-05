package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.omisdk.handlers.CustomAuthAuthenticationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthRegistrationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomAuthPasswordViewModel @Inject constructor(
  private val customAuthRegistrationAction: CustomAuthRegistrationAction,
  private val customAuthAuthenticationAction: CustomAuthAuthenticationAction
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState())
  val uiState = _uiState.asStateFlow()

  private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
  val navigationEvents = _navigationEvents.asSharedFlow()

  data class UiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isRegistrationMode: Boolean = true,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
  )

  sealed class NavigationEvent {
    data object NavigateBack : NavigationEvent()
  }

  sealed class UiEvent {
    data class PasswordChanged(val password: String) : UiEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : UiEvent()
    data object SubmitClicked : UiEvent()
    data object CancelClicked : UiEvent()
  }

  fun setRegistrationMode(isRegistration: Boolean) {
    _uiState.update { it.copy(isRegistrationMode = isRegistration) }
  }

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.PasswordChanged -> {
        _uiState.update { it.copy(password = event.password, errorMessage = null) }
      }

      is UiEvent.ConfirmPasswordChanged -> {
        _uiState.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null) }
      }

      is UiEvent.SubmitClicked -> handleSubmit()
      is UiEvent.CancelClicked -> handleCancel()
    }
  }

  private fun handleSubmit() {
    val state = _uiState.value

    if (state.password.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Password cannot be empty") }
      return
    }

    if (state.isRegistrationMode) {
      // Registration mode - validate confirm password
      if (state.password != state.confirmPassword) {
        _uiState.update { it.copy(errorMessage = "Passwords do not match") }
        return
      }
      if (state.password.length < 4) {
        _uiState.update { it.copy(errorMessage = "Password must be at least 4 characters") }
        return
      }
      // Accept registration with password
      customAuthRegistrationAction.acceptRegistration(state.password)
    } else {
      // Authentication mode - submit password for verification
      customAuthAuthenticationAction.returnSuccess(state.password)
    }

    viewModelScope.launch {
      _navigationEvents.emit(NavigationEvent.NavigateBack)
    }
  }

  private fun handleCancel() {
    val state = _uiState.value

    if (state.isRegistrationMode) {
      customAuthRegistrationAction.denyRegistration()
    } else {
      customAuthAuthenticationAction.returnError(Exception("User cancelled authentication"))
    }

    viewModelScope.launch {
      _navigationEvents.emit(NavigationEvent.NavigateBack)
    }
  }
}
