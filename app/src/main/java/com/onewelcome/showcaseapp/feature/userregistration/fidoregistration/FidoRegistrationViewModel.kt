//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.util.Log
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
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.FidoAuthenticationChallengeHolder
import com.onewelcome.core.omisdk.handlers.FidoRegistrationChallengeHolder
import com.onewelcome.core.usecase.GetTwoStepIdentityProvidersUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.UserRegistrationUseCase
import com.onewelcome.core.util.Constants
import com.onewelcome.data.datastore.ShowcaseDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FidoRegistrationVM"

@HiltViewModel
class FidoRegistrationViewModel @Inject constructor(
    private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
    private val userRegistrationUseCase: UserRegistrationUseCase,
    private val getTwoStepIdentityProvidersUseCase: GetTwoStepIdentityProvidersUseCase,
    private val fidoRegistrationChallengeHolder: FidoRegistrationChallengeHolder,
    private val fidoAuthenticationChallengeHolder: FidoAuthenticationChallengeHolder,
    private val createPinRequestHandler: CreatePinRequestHandler,
    private val showcaseDataStore: ShowcaseDataStore,
) : ViewModel() {

    sealed interface NavigationEvent {
        data object NavigateToFido2BiometricRegistration : NavigationEvent
        data object NavigateToFido2BiometricAuthentication : NavigationEvent
        data object NavigateToCreatePin : NavigationEvent
    }

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    var uiState by mutableStateOf(State())
        private set

    init {
        Log.d(TAG, "ViewModel initialized")
        viewModelScope.launch {
            val sdkInitialized = isSdkInitializedUseCase.execute()
            Log.d(TAG, "SDK initialized: $sdkInitialized")
            uiState = uiState.copy(isSdkInitialized = sdkInitialized)
            updateIdentityProviders()
        }

        // Observe the registration challenge holder: when a challenge arrives, navigate to biometric registration screen
        viewModelScope.launch {
            fidoRegistrationChallengeHolder.pendingChallengeJson.collect { challengeJson ->
                Log.d(TAG, "pendingChallengeJson changed: ${if (challengeJson != null) "PRESENT (${challengeJson.length} chars)" else "NULL"}")
                if (challengeJson != null) {
                    Log.d(TAG, "=== FIDO2 registration challenge received - navigating to FIDO2 biometric registration screen ===")
                    _navigationEvents.send(NavigationEvent.NavigateToFido2BiometricRegistration)
                }
            }
        }

        // Observe the authentication challenge holder: when a challenge arrives, navigate to biometric authentication screen
        viewModelScope.launch {
            fidoAuthenticationChallengeHolder.pendingChallengeJson.collect { challengeJson ->
                Log.d(TAG, "pendingAuthChallengeJson changed: ${if (challengeJson != null) "PRESENT (${challengeJson.length} chars)" else "NULL"}")
                if (challengeJson != null) {
                    Log.d(TAG, "=== FIDO2 authentication challenge received - navigating to FIDO2 biometric authentication screen ===")
                    _navigationEvents.send(NavigationEvent.NavigateToFido2BiometricAuthentication)
                }
            }
        }

        // Listen for OMI SDK PIN creation request: after successful FIDO2 registration, SDK may ask to create a PIN
        viewModelScope.launch {
            createPinRequestHandler.startPinCreationFlow.collect {
                Log.d(TAG, "=== OMI SDK requesting PIN creation - navigating to CreatePin screen ===")
                _navigationEvents.send(NavigationEvent.NavigateToCreatePin)
            }
        }
    }

    fun onEvent(event: UiEvent) {
        Log.d(TAG, "onEvent: $event")
        when (event) {
            is UiEvent.UpdateSelectedIdentityProvider -> {
                Log.d(TAG, "Selected IDP changed: id=${event.identityProvider.id}, name=${event.identityProvider.name}")
                uiState = uiState.copy(selectedIdentityProvider = event.identityProvider)
            }
            is UiEvent.UpdateSelectedScopes -> {
                Log.d(TAG, "Selected scopes changed: ${event.scopes}")
                uiState = uiState.copy(selectedScopes = event.scopes)
            }
            is UiEvent.Register -> register()
            is UiEvent.Authenticate -> authenticate()
        }
    }

    private fun updateIdentityProviders() {
        Log.d(TAG, "Fetching identity providers...")
        getTwoStepIdentityProvidersUseCase.execute()
            .onSuccess { providers ->
                Log.d(TAG, "Identity providers loaded: count=${providers.size}, ids=${providers.map { it.id }}")
                val selected = providers.find { it.id == Constants.IDP_FIDO_CHECK } ?: providers.firstOrNull()
                Log.d(TAG, "Auto-selected IDP: id=${selected?.id}, name=${selected?.name}")
                uiState = uiState.copy(
                    identityProviders = providers,
                    selectedIdentityProvider = selected
                )
            }
            .onFailure {
                Log.e(TAG, "Failed to load identity providers: ${it.message}", it)
                uiState = uiState.copy(identityProviders = emptySet())
            }
    }

    private fun authenticate() {
        val authIdp = uiState.identityProviders.find { it.id == Constants.IDP_FIDO_AUTH }
        if (authIdp == null) {
            Log.e(TAG, "authenticate() called but IDP_FIDO_AUTH ('${Constants.IDP_FIDO_AUTH}') not found in identity providers!")
            uiState = uiState.copy(
                isLoading = false,
                result = Err(Exception("FIDO2 authentication IDP '${Constants.IDP_FIDO_AUTH}' not found. Ensure it is configured on the server."))
            )
            return
        }

        viewModelScope.launch {
            val userId = showcaseDataStore.getFidoUserId().firstOrNull()
            if (userId.isNullOrBlank()) {
                Log.e(TAG, "authenticate() called but no userId stored in DataStore!")
                uiState = uiState.copy(
                    isLoading = false,
                    result = Err(Exception("No FIDO2 userId stored. Please register first before authenticating."))
                )
                return@launch
            }

            Log.d(TAG, "=== STEP 1: Starting OMI SDK FIDO2 authentication ===")
            Log.d(TAG, "Auth IDP: id=${authIdp.id}, name=${authIdp.name}")
            Log.d(TAG, "Stored userId: $userId")
            Log.d(TAG, "Scopes: ${uiState.selectedScopes}")
            uiState = uiState.copy(isLoading = true, result = null)

            Log.d(TAG, "Calling userRegistrationUseCase.register() with auth IDP...")
            userRegistrationUseCase.register(authIdp, uiState.selectedScopes)
                .onSuccess { (userProfile, customInfo) ->
                    Log.d(TAG, "=== OMI SDK FIDO2 authentication COMPLETE (onSuccess) ===")
                    Log.d(TAG, "UserProfile: profileId=${userProfile.profileId}")
                    Log.d(TAG, "CustomInfo: status=${customInfo?.status}, data=${customInfo?.data}")
                    uiState = uiState.copy(isLoading = false, result = Ok(Pair(userProfile, customInfo)))
                }
                .onFailure {
                    Log.e(TAG, "=== OMI SDK FIDO2 authentication FAILED ===")
                    Log.e(TAG, "Error: ${it.message}", it)
                    uiState = uiState.copy(isLoading = false, result = Err(it))
                }
        }
    }

    private fun register() {
        val identityProvider = uiState.selectedIdentityProvider ?: run {
            Log.w(TAG, "register() called but no identity provider selected!")
            return
        }
        Log.d(TAG, "=== STEP 1: Starting OMI SDK registration ===")
        Log.d(TAG, "IDP: id=${identityProvider.id}, name=${identityProvider.name}")
        Log.d(TAG, "Scopes: ${uiState.selectedScopes}")
        uiState = uiState.copy(isLoading = true, result = null)

        viewModelScope.launch {
            Log.d(TAG, "Calling userRegistrationUseCase.register()...")
            Log.d(TAG, "NOTE: OMI SDK will call initRegistration → challenge stored → UI navigates to FIDO2 screen")
            Log.d(TAG, "NOTE: After FIDO2 biometric, OMI SDK resumes → finishRegistration → onSuccess here")
            userRegistrationUseCase.register(identityProvider, uiState.selectedScopes)
                .onSuccess { (userProfile, customInfo) ->
                    Log.d(TAG, "=== OMI SDK registration COMPLETE (onSuccess) ===")
                    Log.d(TAG, "UserProfile: profileId=${userProfile.profileId}")
                    Log.d(TAG, "CustomInfo: status=${customInfo?.status}, data=${customInfo?.data}")

                    // Persist the OMI profileId as the userId (overrides the one saved during biometric)
                    Log.d(TAG, "Persisting OMI profileId='${userProfile.profileId}' to DataStore as userId")
//                    showcaseDataStore.setFidoUserId(userProfile.profileId)
                    Log.d(TAG, "userId persisted to DataStore successfully")

                    uiState = uiState.copy(isLoading = false, result = Ok(Pair(userProfile, customInfo)))
                }
                .onFailure {
                    Log.e(TAG, "=== OMI SDK registration FAILED ===")
                    Log.e(TAG, "Error: ${it.message}", it)
                    uiState = uiState.copy(isLoading = false, result = Err(it))
                }
        }
    }

    data class State(
        val isSdkInitialized: Boolean = false,
        val identityProviders: Set<OneginiIdentityProvider> = emptySet(),
        val selectedIdentityProvider: OneginiIdentityProvider? = null,
        val selectedScopes: List<String> = Constants.DEFAULT_SCOPES,
        val isLoading: Boolean = false,
        val result: Result<Pair<UserProfile, CustomInfo?>, Throwable>? = null,
    )

    sealed interface UiEvent {
        data class UpdateSelectedIdentityProvider(val identityProvider: OneginiIdentityProvider) : UiEvent
        data class UpdateSelectedScopes(val scopes: List<String>) : UiEvent
        data object Register : UiEvent
        data object Authenticate : UiEvent
    }
}
//POC-END
