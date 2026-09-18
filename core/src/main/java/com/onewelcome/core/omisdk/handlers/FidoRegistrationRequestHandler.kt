//POC-START
package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomTwoStepRegistrationAction
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FidoRequestHandler"

/**
 * Handles the two-step FIDO2 registration flow with the OMI SDK.
 *
 * STEP 1 — initRegistration:
 *   - Called by OMI SDK with customInfo = null
 *   - App calls registrationCallback.returnSuccess("") to proceed
 *   - OMI SDK sends this to the server's /init endpoint
 *   - Server responds with the FIDO2 challenge JSON
 *
 * STEP 2 — finishRegistration:
 *   - Called by OMI SDK with customInfo.data = FIDO2 challenge JSON from server
 *   - App stores the challenge JSON + callback in [FidoRegistrationChallengeHolder]
 *   - App navigates to FIDO2 biometric screen
 *   - On biometric success, app calls callback.returnSuccess(fido2ResponseJson)
 *   - OMI SDK sends fido2ResponseJson to server's /complete endpoint
 */
@Singleton
class FidoRegistrationRequestHandler @Inject constructor(
    private val fidoRegistrationChallengeHolder: FidoRegistrationChallengeHolder
) : OneginiCustomTwoStepRegistrationAction {

    override fun initRegistration(
        registrationCallback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        Log.d(TAG, "=== REGISTRATION initRegistration (INIT API) ===")
        Log.d(TAG, "Param sent to server: \"\" (empty string)")
        registrationCallback.returnSuccess("")
    }

    override fun finishRegistration(
        callback: OneginiCustomRegistrationCallback,
        customInfo: CustomInfo?
    ) {
        Log.d(TAG, "=== REGISTRATION finishRegistration (INIT API RESPONSE) ===")
        Log.d(TAG, "customInfo.status: ${customInfo?.status}")
        Log.d(TAG, "customInfo.data (FIDO2 challenge from server): ${customInfo?.data}")

        val challengeJson = customInfo?.data
        if (challengeJson.isNullOrBlank()) {
            Log.e(TAG, "No FIDO2 challenge data received from server!")
            callback.returnError(Exception("No FIDO2 challenge data received from server in finishRegistration"))
            return
        }

        fidoRegistrationChallengeHolder.storeChallenge(challengeJson)
        fidoRegistrationChallengeHolder.storeCallback(callback)
    }
}
//POC-END
