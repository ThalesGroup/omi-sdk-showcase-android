package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomTwoStepRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwoStepRegistrationRequestHandler @Inject constructor() : OneginiCustomTwoStepRegistrationAction {

    private var registrationCallback: OneginiCustomRegistrationCallback? = null

    private val _startTwoStepInputFlow = MutableSharedFlow<TwoStepInputData>(replay = 1)
    val startTwoStepInputFlow: SharedFlow<TwoStepInputData> = _startTwoStepInputFlow.asSharedFlow()

    private val _isRegistrationInProgress = MutableStateFlow(false)
    val isRegistrationInProgress: StateFlow<Boolean> = _isRegistrationInProgress.asStateFlow()

    override fun initRegistration(
        callback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        _isRegistrationInProgress.value = true
        // In the first step, we send initial data to the Token Server
        // This could be any data required to initialize the registration
        callback.returnSuccess("12345")
    }

    override fun finishRegistration(
        callback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        registrationCallback = callback
        // Emit event to navigate to input screen with challenge code
        val challengeCode = customInfo?.data ?: "12345"
        _startTwoStepInputFlow.tryEmit(TwoStepInputData(challengeCode))
    }

    fun submitResponseCode(responseCode: String) {
        registrationCallback?.returnSuccess(responseCode)
        cleanUp()
    }

    fun cancelRegistration() {
        registrationCallback?.returnError(Exception("Registration canceled by user"))
        cleanUp()
    }

    fun isInProgress(): Boolean = _isRegistrationInProgress.value

    private fun cleanUp() {
        registrationCallback = null
        _isRegistrationInProgress.value = false
        _startTwoStepInputFlow.resetReplayCache()
    }

    data class TwoStepInputData(val challengeCode: String)
}
