package com.onewelcome.showcaseapp.feature.resourcecall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.usecase.resourcecall.AnonymousResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.ImplicitResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UnauthenticatedResourceCallUseCase
import com.onewelcome.core.usecase.resourcecall.UserAuthenticatedResourceCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourceCallViewModel @Inject constructor(
    private val unauthenticatedUseCase: UnauthenticatedResourceCallUseCase,
    private val anonymousUseCase: AnonymousResourceCallUseCase,
    private val userAuthenticatedUseCase: UserAuthenticatedResourceCallUseCase,
    private val implicitUseCase: ImplicitResourceCallUseCase
) : ViewModel() {

    var uiState by mutableStateOf(State())
        private set

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectResourceType -> {
                uiState = uiState.copy(selectedResourceType = event.type)
            }
            is UiEvent.ExecuteUnauthenticatedCall -> executeUnauthenticatedCall()
            is UiEvent.ExecuteAnonymousCall -> executeAnonymousCall()
            is UiEvent.ExecuteUserAuthenticatedCall -> executeUserAuthenticatedCall()
            is UiEvent.ExecuteImplicitCall -> executeImplicitCall()
            is UiEvent.ClearResult -> uiState = uiState.copy(result = null, errorMessage = null)
        }
    }
    private fun executeUnauthenticatedCall() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                result = null,
                errorMessage = null,
                currentStep = "Step 1: Creating unauthenticated API client..."
            )

            uiState = uiState.copy(currentStep = "Step 2: Sending request (no token)...")
            
            unauthenticatedUseCase.getPathResources()
                .onSuccess { 
                    uiState = uiState.copy(
                        isLoading = false,
                        result = "✅ Unauthenticated Call Success!\n\n" ,
                        currentStep = null
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Unauthenticated Call Failed!\n\n" +
                                "Error: ${error.message}\n\n" +
                                "This call doesn't require any authentication.",
                        currentStep = null
                    )
                }
        }
    }
    private fun executeAnonymousCall() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                result = null,
                errorMessage = null,
                currentStep = "Step 1: Authenticating device..."
            )

            anonymousUseCase.authenticateDevice()
                .onSuccess {
                    uiState = uiState.copy(currentStep = "Step 2: Device authenticated! Fetching app details...")
                    
                    anonymousUseCase.authenticateAndGetAppDetails(arrayOf("application-details"))
                        .onSuccess { appDetails ->
                            uiState = uiState.copy(
                                isLoading = false,
                                result = "Anonymous Call Success!\n\n" +
                                        "Version: ${appDetails.application_version}\n" +
                                        "Platform: ${appDetails.application_platform}\n" +
                                        "Identifier: ${appDetails.application_identifier}",
                                currentStep = null
                            )
                        }
                        .onFailure { error ->
                            uiState = uiState.copy(
                                isLoading = false,
                                errorMessage = "Anonymous Resource Call Failed!\n\n" +
                                        "Device was authenticated but API call failed.\n" +
                                        "Error: ${error.message}\n\n" ,
                                currentStep = null
                            )
                        }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Device Authentication Failed!\n\n" +
                                "Error: ${error.message}\n\n" ,
                        currentStep = null
                    )
                }
        }
    }
    private fun executeUserAuthenticatedCall() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                result = null,
                errorMessage = null,
                currentStep = "Step 1: Checking user authentication..."
            )

            uiState = uiState.copy(currentStep = "Step 2: Retrieving user access token...")
            uiState = uiState.copy(currentStep = "Step 3: Fetching user profile...")
            
            userAuthenticatedUseCase.getDeviceList()
                .onSuccess { deviceList ->
                    uiState = uiState.copy(
                        isLoading = false,
                        result = "User Authenticated Call Success!\n\n" +
                                "Devices:"+ deviceList.devices.joinToString("\n") { it.name },
                        currentStep = null
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "User Authenticated Call Failed!\n\n" +
                                "Error: ${error.message}\n\n",
                        currentStep = null
                    )
                }
        }
    }
    private fun executeImplicitCall() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                result = null,
                errorMessage = null,
                currentStep = "Step 1: Checking registered user profiles..."
            )

            // Get available user profiles
            val userProfiles = implicitUseCase.getUserProfiles()
            
            if (userProfiles.isEmpty()) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "No Registered Users!\n\n" +
                            "Implicit resource calls require a registered user.\n" +
                            "User doesn't need to be logged in, but must have registered at least once.\n\n" +
                            "Please register a user first, then try again.",
                    currentStep = null
                )
                return@launch
            }

            // Use the first available user profile
            val userProfile = userProfiles.first()
            
            uiState = uiState.copy(currentStep = "Step 2: Authenticating user implicitly (no PIN)...")
            
            implicitUseCase.authenticateUserImplicitly(userProfile)
                .onSuccess {
                    uiState = uiState.copy(currentStep = "Step 3: Implicit auth success! Fetching basic profile...")
                    
                    implicitUseCase.getUserId()
                        .onSuccess { userId ->
                            uiState = uiState.copy(
                                isLoading = false,
                                result = "Implicit Call Success!\n\n"+
                                "User Id:"+userId.decorated_user_id ,
                                currentStep = null
                            )
                        }
                        .onFailure { error ->
                            uiState = uiState.copy(
                                isLoading = false,
                                errorMessage = "Implicit Resource Call Failed!\n\n" +
                                        "Implicit auth succeeded but API call failed.\n" +
                                        "Error: ${error.message}\n\n",
                                currentStep = null
                            )
                        }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Implicit Authentication Failed!\n\n" +
                                "Error: ${error.message}\n\n",
                        currentStep = null
                    )
                }
        }
    }

    data class State(
        val selectedResourceType: ResourceType = ResourceType.UNAUTHENTICATED,
        val isLoading: Boolean = false,
        val currentStep: String? = null,
        val result: String? = null,
        val errorMessage: String? = null
    )

    sealed interface UiEvent {
        data class SelectResourceType(val type: ResourceType) : UiEvent
        data object ExecuteUnauthenticatedCall : UiEvent
        data object ExecuteAnonymousCall : UiEvent
        data object ExecuteUserAuthenticatedCall : UiEvent
        data object ExecuteImplicitCall : UiEvent
        data object ClearResult : UiEvent
    }
}

enum class ResourceType(val title: String, val description: String) {
    UNAUTHENTICATED(
        title = "Unauthenticated",
        description = "No token required."
    ),
    ANONYMOUS(
        title = "Anonymous (Device Auth)",
        description = "Device token required. Call authenticateDevice() first, then make API calls."
    ),
    USER_AUTHENTICATED(
        title = "User Authenticated",
        description = "User token required. User must be logged in with PIN/Biometric."
    ),
    IMPLICIT(
        title = "Implicit (User Registered)",
        description = "Implicit token. User must be registered but NO PIN required. For read-only data."
    )
}
