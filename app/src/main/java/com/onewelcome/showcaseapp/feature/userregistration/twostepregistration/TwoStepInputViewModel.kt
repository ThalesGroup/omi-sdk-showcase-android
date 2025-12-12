package com.onewelcome.showcaseapp.feature.userregistration.twostepregistration

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
import com.onegini.mobile.sdk.android.handlers.error.OneginiRegistrationError
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.TwoStepRegistrationRequestHandler
import com.onewelcome.core.usecase.GetTwoStepIdentityProvidersUseCase
import com.onewelcome.core.usecase.GetUserProfilesUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwoStepInputViewModel @Inject constructor(
    private val userRegistrationUseCase: UserRegistrationUseCase,
    private val getTwoStepIdentityProvidersUseCase: GetTwoStepIdentityProvidersUseCase,
    private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler,
    private val createPinRequestHandler: CreatePinRequestHandler,
) : ViewModel() {

    var uiState by mutableStateOf(State())
        private set

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        listenForTwoStepInputFlow()
        listenForPinScreenNavigationEvent()
        listenForRegistrationResult()
        startTwoStepRegistration()
    }

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SubmitResponseCode -> submitResponseCode(event.responseCode)
            is UiEvent.CancelRegistration -> cancelRegistration()
            is UiEvent.UpdateResponseCode -> uiState = uiState.copy(responseCode = event.code)
        }
    }

    private fun startTwoStepRegistration() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, isRegistrationInProgress = true)
            
            // Get the first two-step identity provider
            val identityProvider = getTwoStepIdentityProvider()
            if (identityProvider == null) {
                uiState = uiState.copy(
                    isLoading = false,
                    isRegistrationInProgress = false,
                    result = Err(Exception("No Two-Step identity provider available"))
                )
                return@launch
            }

            // Start registration in background
            userRegistrationUseCase
                .register(identityProvider = identityProvider, scopes = Constants.DEFAULT_SCOPES)
                .onSuccess { handleSuccess(it) }
                .onFailure { handleFailure(it) }
        }
    }

    private fun getTwoStepIdentityProvider(): OneginiIdentityProvider? {
        var provider: OneginiIdentityProvider? = null
        getTwoStepIdentityProvidersUseCase.execute()
            .onSuccess { providers -> provider = providers.firstOrNull() }
        return provider
    }

    private fun listenForTwoStepInputFlow() {
        viewModelScope.launch {
            twoStepRegistrationRequestHandler.startTwoStepInputFlow.collect { inputData ->
                uiState = uiState.copy(
                    challengeCode = inputData.challengeCode,
                    isLoading = false
                )
            }
        }
    }

    private fun listenForPinScreenNavigationEvent() {
        viewModelScope.launch {
            createPinRequestHandler.startPinCreationFlow.collect {
                _navigationEvents.send(NavigationEvent.ToPinScreen)
            }
        }
    }

    private fun listenForRegistrationResult() {
        viewModelScope.launch {
            // The registration result will be handled in handleSuccess/handleFailure
        }
    }

    private fun submitResponseCode(responseCode: String) {
        uiState = uiState.copy(isLoading = true)
        twoStepRegistrationRequestHandler.submitResponseCode(responseCode)
    }

    private fun cancelRegistration() {
        viewModelScope.launch {
            twoStepRegistrationRequestHandler.cancelRegistration()
            createPinRequestHandler.cancelPinCreation()
            uiState = uiState.copy(isRegistrationInProgress = false)
            _navigationEvents.send(NavigationEvent.GoBack)
        }
    }

    private suspend fun handleSuccess(pair: Pair<UserProfile, CustomInfo?>) {
        uiState = uiState.copy(
            result = Ok(pair),
            isLoading = false,
            isRegistrationInProgress = false
        )
        _navigationEvents.send(NavigationEvent.RegistrationComplete)
    }

    private fun handleFailure(throwable: Throwable) {
        val isActionAlreadyInProgressError =
            throwable is OneginiRegistrationError && throwable.errorType == OneginiRegistrationError.Type.ACTION_ALREADY_IN_PROGRESS
        uiState = if (isActionAlreadyInProgressError) {
            uiState.copy(result = Err(throwable), isLoading = false)
        } else {
            uiState.copy(result = Err(throwable), isLoading = false, isRegistrationInProgress = false)
        }
    }

    data class State(
        val challengeCode: String = "",
        val responseCode: String = "",
        val isLoading: Boolean = true,
        val isRegistrationInProgress: Boolean = false,
        val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null
    )

    sealed interface UiEvent {
        data class SubmitResponseCode(val responseCode: String) : UiEvent
        data class UpdateResponseCode(val code: String) : UiEvent
        data object CancelRegistration : UiEvent
    }

    sealed interface NavigationEvent {
        data object ToPinScreen : NavigationEvent
        data object GoBack : NavigationEvent
        data object RegistrationComplete : NavigationEvent
    }
}
