//POC-START
package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FidoAuthChallengeHolder"

/**
 * Singleton holder that stores data received during the FIDO2 two-step authentication flow.
 *
 * Flow:
 * 1. OMI SDK calls initAuthentication → app sends userId via callback.returnSuccess(userId)
 * 2. Server responds with FIDO2 challenge JSON
 * 3. OMI SDK calls finishAuthentication with customInfo.data = FIDO2 challenge JSON
 *    → [storeChallenge] stores the challenge JSON
 *    → [storeCallback] stores the finishAuthentication callback
 *    → UI is notified via [pendingChallengeJson] StateFlow
 * 4. UI calls Thales FIDO2 SDK with the challenge JSON → biometric/PIN prompt shown
 * 5. Thales FIDO2 SDK returns response JSON
 *    → UI calls [pendingCallback].returnSuccess(fido2ResponseJson) to resume OMI SDK
 *    → OMI SDK sends fido2ResponseJson to server via /complete API
 * 6. Authentication complete
 */
@Singleton
class FidoAuthenticationChallengeHolder @Inject constructor() {

    /** The FIDO2 challenge JSON received from the server in finishAuthentication's customInfo.data */
    private val _pendingChallengeJson = MutableStateFlow<String?>(null)
    val pendingChallengeJson: StateFlow<String?> = _pendingChallengeJson.asStateFlow()

    /**
     * The OMI SDK callback from finishAuthentication.
     * Must be called with the Thales FIDO2 SDK response JSON to resume the OMI SDK flow.
     */
    @Volatile
    var pendingCallback: OneginiCustomRegistrationCallback? = null
        private set

    /**
     * Called from finishAuthentication to store the FIDO2 challenge JSON from customInfo.data.
     */
    fun storeChallenge(challengeJson: String?) {
        Log.d(TAG, "Storing FIDO2 auth challenge JSON ($challengeJson chars)")
        _pendingChallengeJson.value = challengeJson
    }

    /**
     * Called from finishAuthentication to store the OMI SDK callback.
     */
    fun storeCallback(callback: OneginiCustomRegistrationCallback) {
        Log.d(TAG, "Storing finishAuthentication callback")
        pendingCallback = callback
    }

    /**
     * Clears all stored data (challenge JSON and callback).
     */
    fun clear() {
        Log.d(TAG, "Clearing auth challenge holder (challengeJson and callback)")
        _pendingChallengeJson.value = null
        pendingCallback = null
    }
}
//POC-END
