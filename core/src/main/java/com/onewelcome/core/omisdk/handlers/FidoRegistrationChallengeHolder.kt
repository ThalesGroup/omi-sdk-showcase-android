//POC-START
package com.onewelcome.core.omisdk.handlers

import android.util.Log
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiCustomRegistrationCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FidoChallengeHolder"

/**
 * Singleton holder that stores data received during the FIDO2 two-step registration flow.
 *
 * Corrected flow:
 * 1. OMI SDK calls initRegistration → customInfo.data = FIDO2 challenge JSON from server
 *    → [storeChallenge] stores the challenge JSON
 *    → [storeCallback] stores the initRegistration callback (needed to resume OMI SDK flow)
 *    → UI is notified via [pendingChallengeJson] StateFlow
 * 2. UI calls Thales FIDO2 SDK with the challenge JSON → biometric/PIN prompt shown
 * 3. Thales FIDO2 SDK returns response JSON
 *    → UI calls [pendingCallback].returnSuccess(fido2ResponseJson) to resume OMI SDK
 *    → OMI SDK sends fido2ResponseJson to server via /complete API
 * 4. OMI SDK calls finishRegistration → registration complete
 * 5. OMI SDK registration succeeds → userId persisted to ShowcaseDataStore (survives app restart)
 */
@Singleton
class FidoRegistrationChallengeHolder @Inject constructor() {

    /** The FIDO2 challenge JSON received from the server in initRegistration's customInfo.data */
    private val _pendingChallengeJson = MutableStateFlow<String?>(null)
    val pendingChallengeJson: StateFlow<String?> = _pendingChallengeJson.asStateFlow()

    /**
     * The OMI SDK callback from initRegistration.
     * Must be called with the Thales FIDO2 SDK response JSON to resume the OMI SDK flow.
     * This is NOT a StateFlow because callbacks are not serializable/observable.
     */
    @Volatile
    var pendingCallback: OneginiCustomRegistrationCallback? = null
        private set

    /**
     * Called from initRegistration to store the FIDO2 challenge JSON from customInfo.data.
     */
    fun storeChallenge(challengeJson: String) {
        Log.d(TAG, "Storing FIDO2 challenge JSON (${challengeJson.length} chars)")
        _pendingChallengeJson.value = challengeJson
    }

    /**
     * Called from initRegistration to store the OMI SDK callback.
     * The UI must call [pendingCallback].returnSuccess(fido2ResponseJson) after
     * the Thales FIDO2 SDK biometric prompt completes successfully.
     */
    fun storeCallback(callback: OneginiCustomRegistrationCallback) {
        Log.d(TAG, "Storing initRegistration callback: ${callback.javaClass.simpleName}")
        pendingCallback = callback
    }

    /**
     * Clears all stored data (challenge JSON and callback).
     * Call this after the registration flow completes (success or failure).
     */
    fun clear() {
        Log.d(TAG, "Clearing challenge holder (challengeJson and callback)")
        _pendingChallengeJson.value = null
        pendingCallback = null
    }
}
//POC-END
