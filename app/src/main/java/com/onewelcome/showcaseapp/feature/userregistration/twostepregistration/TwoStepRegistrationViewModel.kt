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
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.StatelessUserRegistrationUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwoStepRegistrationViewModel @Inject constructor(
    isSdkInitializedUseCase: IsSdkInitializedUseCase,
    private val userRegistrationUseCase: UserRegistrationUseCase,
    private val statelessUserRegistrationUseCase: StatelessUserRegistrationUseCase,
    private val getTwoStepIdentityProvidersUseCase: GetTwoStepIdentityProvidersUseCase,
    private val getUserProfilesUseCase: GetUserProfilesUseCase,
    private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler,
    private val createPinRequestHandler: CreatePinRequestHandler,
) : ViewModel() {

    var uiState by mutableStateOf(State())
        private set

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            isSdkInitializedUseCase.execute().let { uiState = uiState.copy(isSdkInitialized = it) }
            updateIdentityProviders()
            updateSelectedIdentityProvider()
            updateUserProfiles()
            updateCancellationButton()
        }
    }

    private fun enterOptionalData() {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.ToTwoStepInputScreen)
        }
    }

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.UpdateSelectedIdentityProvider -> uiState = uiState.copy(selectedIdentityProvider = event.identityProvider)
            is UiEvent.UpdateSelectedScopes -> uiState = uiState.copy(selectedScopes = event.scopes)
            is UiEvent.CancelRegistration -> cancelRegistration()
            is UiEvent.SetStatelessRegistration -> {
                startRegistration()
                uiState = uiState.copy(isStatelessRegistration = event.isStateless)
            }
            is UiEvent.SubmitResponseCode -> submitResponseCode(event.responseCode)
            is UiEvent.SubmitOptionalData -> submitOptionalData(event.optionalData)
            is UiEvent.EnterOptionalData -> enterOptionalData()
        }
    }

    private fun submitOptionalData(optionalData: String) {
        uiState = uiState.copy(isOptionalDataSubmit = true)
        twoStepRegistrationRequestHandler.setOptionalData(optionalData)
        startRegistration()
    }

    private fun updateIdentityProviders() {
        getTwoStepIdentityProvidersUseCase.execute()
            .onSuccess { uiState = uiState.copy(identityProviders = it) }
            .onFailure { uiState = uiState.copy(identityProviders = emptySet()) }
    }

    private suspend fun updateUserProfiles() {
        getUserProfilesUseCase.execute()
            .onSuccess { uiState = uiState.copy(userProfileIds = it.map { it.profileId }.toList()) }
            .onFailure { uiState = uiState.copy(userProfileIds = emptyList()) }
    }

    private fun updateSelectedIdentityProvider() {
        val identityProviders = uiState.identityProviders
        if (identityProviders.isNotEmpty()) {
            uiState = uiState.copy(selectedIdentityProvider = identityProviders.first())
        }
    }

    private fun updateCancellationButton() {
        val isRegistrationInProgress =
            createPinRequestHandler.isPinCreationInProgress() && twoStepRegistrationRequestHandler.isTwoStepregistrationInProgress()
        uiState = uiState.copy(isRegistrationCancellationEnabled = isRegistrationInProgress)
    }

    private fun cancelRegistration() {
        viewModelScope.launch {
            twoStepRegistrationRequestHandler.cancelRegistration()
            createPinRequestHandler.cancelPinCreation()
        }
    }

    private fun startRegistration() {
        if (uiState.isStatelessRegistration) {
            registerStatelessUser()
        } else {
            listenForTwoStepInputNavigationEvent()
            listenForPinScreenNavigationEvent()
            registerUser()
        }
    }

    private fun listenForTwoStepInputNavigationEvent() {
        viewModelScope.launch {
            twoStepRegistrationRequestHandler.startTwoStepInputFlow.collect { inputData ->
                uiState = uiState.copy(challengeCode = inputData.challengeCode, isOptionalDataSubmit = false)
                _navigationEvents.send(NavigationEvent.ToTwoStepVerificationScreen)
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

    private fun registerStatelessUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isRegistrationCancellationEnabled = true)
            statelessUserRegistrationUseCase
                .execute(identityProvider = getIdentityProvider(), scopes = uiState.selectedScopes)
                .onSuccess { handleSuccess(UserProfile.stateless to it) }
                .onFailure { handleFailure(it) }
        }
    }

    private fun registerUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isRegistrationCancellationEnabled = true)
            userRegistrationUseCase
                .register(identityProvider = getIdentityProvider(), scopes = uiState.selectedScopes)
                .onSuccess { handleSuccess(it) }
                .onFailure { handleFailure(it) }
        }
    }

    private fun submitResponseCode(responseCode: String) {
        twoStepRegistrationRequestHandler.submitResponseCode(responseCode)
    }

    private suspend fun handleSuccess(pair: Pair<UserProfile, CustomInfo?>) {
        _navigationEvents.trySend(NavigationEvent.ToTwoStepRegistrationScreen)
        uiState = uiState.copy(result = Ok(pair), isRegistrationCancellationEnabled = false)
        updateUserProfiles()
    }

    private fun handleFailure(throwable: Throwable) {
        val isActionAlreadyInProgressError =
            throwable is OneginiRegistrationError && throwable.errorType == OneginiRegistrationError.Type.ACTION_ALREADY_IN_PROGRESS
        uiState = if (isActionAlreadyInProgressError) {
            uiState.copy(result = Err(throwable))
        } else {
            uiState.copy(result = Err(throwable), isRegistrationCancellationEnabled = false)
        }
    }

    private fun getIdentityProvider(): OneginiIdentityProvider? = uiState.selectedIdentityProvider

    data class State(
        val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
        val identityProviders: Set<OneginiIdentityProvider> = emptySet(),
        val isSdkInitialized: Boolean = false,
        val selectedIdentityProvider: OneginiIdentityProvider? = null,
        val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
        val userProfileIds: List<String> = emptyList(),
        val isRegistrationCancellationEnabled: Boolean = false,
        val isStatelessRegistration: Boolean = false,
        val challengeCode: String = "",
        val responseCode: String = "",
        val isOptionalDataSubmit: Boolean = false,
    )

    sealed interface UiEvent {
        data object EnterOptionalData : UiEvent

        data object CancelRegistration : UiEvent
        data class SubmitOptionalData(val optionalData: String) : UiEvent
        data class UpdateSelectedIdentityProvider(val identityProvider: OneginiIdentityProvider) : UiEvent
        data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
        data class SetStatelessRegistration(val isStateless: Boolean) : UiEvent
        data class SubmitResponseCode(val responseCode: String) : UiEvent
    }

    sealed interface NavigationEvent {
        data object ToTwoStepRegistrationScreen : NavigationEvent
        data object ToPinScreen : NavigationEvent
        data object ToTwoStepInputScreen : NavigationEvent
        data object ToTwoStepVerificationScreen : NavigationEvent
    }
}
