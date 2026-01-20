package com.onewelcome.showcaseapp.feature.tokens

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.usecase.GetAuthenticatedUserProfileUseCase
import com.onewelcome.core.usecase.GetIdTokenUseCase
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TokensViewModel @Inject constructor(
    private val isSdkInitializedUseCase: IsSdkInitializedUseCase,
    private val getAuthenticatedUserProfileUseCase: GetAuthenticatedUserProfileUseCase,
    private val getIdTokenUseCase: GetIdTokenUseCase
) : ViewModel() {

    var uiState by mutableStateOf(State())
        private set

    init {
        updateIsSdkInitialized()
        updateAuthenticatedUserProfile()
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.GetIdToken -> {
                val result = getIdTokenUseCase.execute()
                uiState = uiState.copy(idTokenResult = result)
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

    fun getFormattedIdToken(idToken: String): String {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = parts[1]
                val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
                val decodedJson = String(decodedBytes, Charsets.UTF_8)
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(JsonParser.parseString(decodedJson))
            } else {
                idToken
            }
        } catch (e: Exception) {
            idToken
        }
    }

    data class State(
        val isSdkInitialized: Boolean = false,
        val userProfile: UserProfile? = null,
        val idTokenResult: Result<String, Throwable>? = null,
    )

    sealed class Event {
        data object GetIdToken : Event()
    }
}
