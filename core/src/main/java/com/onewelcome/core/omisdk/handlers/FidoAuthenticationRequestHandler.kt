//POC-START
package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomTwoStepRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FidoAuthRequestHandler"

/**
 * Handles the two-step FIDO2 authentication flow with the OMI SDK.
 *
 * STEP 1 — initRegistration:
 *   - Called by OMI SDK with customInfo = null
 *   - App calls registrationCallback.returnSuccess("") to proceed
 *   - OMI SDK sends this to the server's /init endpoint
 *
 * STEP 2 — finishRegistration:
 *   - Called by OMI SDK with customInfo.data = FIDO2 challenge JSON from server
 *   - App stores the challenge JSON + callback in [FidoAuthenticationChallengeHolder]
 *   - App navigates to FIDO2 biometric screen
 *   - On biometric success, app calls callback.returnSuccess(fido2ResponseJson + userId)
 *   - OMI SDK sends payload to server's /complete endpoint
 */
@Singleton
class FidoAuthenticationRequestHandler @Inject constructor(
    private val fidoAuthenticationChallengeHolder: FidoAuthenticationChallengeHolder,
    private val showcaseDataStore: ShowcaseDataStore,
) : OneginiCustomTwoStepRegistrationAction {

    override fun initRegistration(
        registrationCallback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        Log.d(TAG, "=== AUTHENTICATION initRegistration (INIT API) ===")
        val userId = runBlocking { showcaseDataStore.getFidoUserId().firstOrNull() ?: "" }
        val payload = "{\"userId\": \"$userId\"}"
        Log.d(TAG, "Param sent to server (JSON): $payload")
        registrationCallback.returnSuccess(payload)
    }

    override fun finishRegistration(
        callback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        Log.d(TAG, "=== AUTHENTICATION finishRegistration (INIT API RESPONSE) ===")
        Log.d(TAG, "customInfo.status: $customInfo")
        Log.d(TAG, "customInfo.data (FIDO2 challenge from server): ${customInfo?.data}")

        val challengeJson = customInfo?.data
        if (challengeJson.isNullOrBlank()) {
            Log.e(TAG, "No FIDO2 challenge data received from server!")
            callback.returnError(Exception("No FIDO2 challenge data received from server in finishAuthentication"))
            return
        }

        fidoAuthenticationChallengeHolder.storeChallenge(challengeJson)
        fidoAuthenticationChallengeHolder.storeCallback(callback)
    }
}
//POC-END
