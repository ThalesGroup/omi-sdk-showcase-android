package com.onewelcome.core.omisdk.handlers

import com.onegini.mobile.sdk.android.handlers.request.OneginiMobileAuthWithPushCustomRequestHandler
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomCallback
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthWithPushCustomRequestHandler @Inject constructor() : OneginiMobileAuthWithPushCustomRequestHandler {

     var customCallback: OneginiCustomCallback? = null

    private val _startCustomAuthenticationFlow = Channel<CustomAuthenticationData>(Channel.BUFFERED)
    val startCustomAuthenticationFlow = _startCustomAuthenticationFlow.receiveAsFlow()

    override fun startAuthentication(
      mobileAuthenticationRequest: OneginiMobileAuthenticationRequest,
        callback: OneginiCustomCallback
    ) {
        customCallback = callback
        _startCustomAuthenticationFlow.trySend(
            CustomAuthenticationData(
                message = mobileAuthenticationRequest.message,
                userProfileId = mobileAuthenticationRequest.userProfile,
                challengeData = mobileAuthenticationRequest.signingData,
                challengeStatus = mobileAuthenticationRequest.transactionId.toInt()
            )
        )
    }

    override fun finishAuthentication() {
        customCallback = null
    }





    /**
     * Data class containing information about the custom authentication request.
     */
    data class CustomAuthenticationData(
        val message: String?,
        val userProfileId: UserProfile,
        val challengeData: String?,
        val challengeStatus: Int?
    )
}
