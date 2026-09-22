//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.app.Activity
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.json.JSONException
import org.json.JSONObject
import com.onewelcome.core.omisdk.handlers.FidoRegistrationChallengeHolder
import com.onewelcome.data.datastore.ShowcaseDataStore
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.authenticator.biometric.BiometricAuthenticatorCallback
import com.thalesgroup.gemalto.fido2.client.AuthenticatorDescriptionCallback
import com.thalesgroup.gemalto.fido2.client.AuthenticatorSelectionCallback
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorInfo
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.VerifyMethod
import com.thalesgroup.gemalto.fido2.client.Fido2Config
import com.thalesgroup.gemalto.fido2.client.Fido2Request
import com.thalesgroup.gemalto.fido2.client.Fido2RespondArgs
import com.thalesgroup.gemalto.fido2.client.Fido2Response
import com.thalesgroup.gemalto.fido2.client.Fido2ResponseCallback
import com.thalesgroup.gemalto.fido2.client.Fido2UiCallback
import com.thalesgroup.gemalto.fido2.client.TransactionDetailsCallback
import com.thalesgroup.gemalto.securelog.SecureLogConfig
import com.thalesgroup.gemalto.securelog.SecureLogLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "Fido2BiometricVM"

/**
 * ViewModel for the FIDO2 biometric registration screen.
 *
 * Flow:
 * 1. OMI SDK calls initRegistration → challenge JSON + callback stored in [FidoRegistrationChallengeHolder]
 * 2. FidoRegistrationViewModel detects pending challenge → navigates to this screen
 * 3. This VM calls Thales FIDO2 SDK with the challenge JSON → biometric prompt shown
 * 4. Thales FIDO2 SDK returns response JSON
 *    → Wrapped as plain JSON object: {"fido2": {<responseObject>}, "userId": "<userId>"}
 *    → Call stored OMI SDK callback with payload → OMI SDK sends to server /complete API
 * 5. OMI SDK calls finishRegistration → registration complete
 */
@HiltViewModel
class Fido2BiometricRegistrationViewModel @Inject constructor(
    private val fidoRegistrationChallengeHolder: FidoRegistrationChallengeHolder,
    private val showcaseDataStore: ShowcaseDataStore,
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object BiometricInProgress : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
        data class AuthenticatorSelection(
            val selectableAuthenticators: List<Fido2AuthenticatorInfo>,
            val allAuthenticators: List<Fido2AuthenticatorInfo>,
            val callback: AuthenticatorSelectionCallback
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val pendingChallengeJson = fidoRegistrationChallengeHolder.pendingChallengeJson

    /**
     * Extracts the inner FIDO2 PublicKeyCredentialCreationOptions JSON from the server response.
     * The server wraps the FIDO2 options inside a "fido2" key along with a "userId".
     * Also strips https:// from rpId if present (FIDO2 spec requires plain hostname).
     */
    private fun extractFido2OptionsJson(wrappedJson: String): Pair<String, String?> {
        return try {
            val root = JSONObject(wrappedJson)
            val userId = if (root.has("userId")) root.getString("userId") else null

            val fido2Object = if (root.has("fido2")) {
                root.getJSONObject("fido2")
            } else {
                root
            }

            if (fido2Object.has("rp")) {
                val rp = fido2Object.getJSONObject("rp")
                if (rp.has("id")) {
                    val rpId = rp.getString("id")
                    val cleanRpId = rpId.removePrefix("https://").removePrefix("http://").trimEnd('/')
                    if (cleanRpId != rpId) {
                        rp.put("id", cleanRpId)
                    }
                }
            }

            Pair(fido2Object.toString(), userId)
        } catch (e: JSONException) {
            Log.w(TAG, "Could not parse wrapper JSON, using raw JSON: ${e.message}")
            Pair(wrappedJson, null)
        }
    }

    /**
     * Starts the Thales FIDO2 SDK registration using the stored challenge JSON.
     *
     * @param activity Required by Thales FIDO2 SDK to show the biometric prompt.
     * @param challengeJson The wrapped server JSON containing the FIDO2 challenge.
     */
    fun startFido2Registration(activity: Activity, challengeJson: String) {
        Log.d(TAG, "=== REGISTRATION: INIT API RESPONSE (challenge received) ===")
        Log.d(TAG, "Challenge JSON: $challengeJson")

        _uiState.value = UiState.BiometricInProgress

        try {
            val fido2Client = Fido2ClientFactory.createFido2Client(activity)
            if (activity is FragmentActivity) {
                fido2Client.setActivity(activity)
            }

            val (fido2OptionsJson, extractedUserId) = extractFido2OptionsJson(challengeJson)

            val fido2Request = Fido2Request.jsonText(fido2OptionsJson)

            val respondArgs = Fido2RespondArgs.Builder()
                .setFido2Request(fido2Request)
                .setUiCallback(object : Fido2UiCallback() {
                    override fun showAuthenticators(
                        authenticators: List<Fido2AuthenticatorInfo>,
                        callback: AuthenticatorSelectionCallback
                    ) {
                        val selectable = authenticators.filterNot { it.verifyMethod == VerifyMethod.PLATFORM }
                        if (selectable.isEmpty()) {
                            callback.cancel()
                            _uiState.value = UiState.Error(
                                "No supported authenticator available.\n" +
                                "The server only offers passkeys (PLATFORM) which requires " +
                                "Google Play Services credential provider."
                            )
                        } else if (selectable.size == 1) {
                            val selectedIndex = authenticators.indexOf(selectable[0])
                            _uiState.value = UiState.BiometricInProgress
                            callback.onAuthenticatorSelected(selectedIndex)
                        } else {
                            _uiState.value = UiState.AuthenticatorSelection(
                                selectableAuthenticators = selectable,
                                allAuthenticators = authenticators,
                                callback = callback
                            )
                        }
                    }

                    override fun retrieveAuthenticatorDescription(
                        authenticator: Fido2AuthenticatorInfo,
                        callback: AuthenticatorDescriptionCallback
                    ) {
                        callback.cancel()
                    }

                    override fun showTransactionDetails(
                        details: Map<String, String>,
                        callback: TransactionDetailsCallback
                    ) {
                        callback.onProceed()
                    }
                })
                .setBiometricAuthenticatorCallback(BiometricAuthenticatorCallback { _ ->
                    "Verify your identity to register"
                })
                .build()

            try {
                val dummyModulus = ByteArray(256) { 0x01 }
                val dummyExponent = byteArrayOf(0x01, 0x00, 0x01)
                val secureLogConfig = SecureLogConfig.Builder(activity)
                    .fileID("fido2-poc")
                    .publicKey(dummyModulus, dummyExponent)
                    .rollingFileMaxCount(1)
                    .rollingFileMaxSizeInKB(100)
                    .directory(activity.cacheDir)
                    .level(SecureLogLevel.OFF)
                    .build()
                Fido2Config.setUpSecureLog(secureLogConfig)
            } catch (e: Exception) {
                Log.w(TAG, "SecureLog setup warning (non-fatal): ${e.message}")
            }

            fido2Client.respondWithArgs(respondArgs, object : Fido2ResponseCallback {
                override fun onResponded(response: Fido2Response) {
                    val responseJson = response.raw() ?: ""

                    // Persist the userId to DataStore so it survives app restarts
                    if (!extractedUserId.isNullOrBlank()) {
                        viewModelScope.launch {
                            showcaseDataStore.setFidoUserId(extractedUserId)
                        }
                    }

                            val payload = buildString {
                        append("{")
                        append("\"fido2\":")
                        append(responseJson)
                        if (!extractedUserId.isNullOrBlank()) {
                            append(",\"userId\":\"")
                            append(extractedUserId)
                            append("\"")
                        }
                        append("}")
                    }

                    Log.d(TAG, "=== REGISTRATION: COMPLETE API PAYLOAD ===")
                    Log.d(TAG, "Payload: $payload")

                    val callback = fidoRegistrationChallengeHolder.pendingCallback
                    if (callback != null) {
                        callback.returnSuccess(payload)
                    } else {
                        Log.e(TAG, "OMI SDK callback is null — cannot resume registration flow")
                        _uiState.value = UiState.Error("Internal error: OMI SDK callback lost")
                        fidoRegistrationChallengeHolder.clear()
                        return
                    }

                    fidoRegistrationChallengeHolder.clear()
                    _uiState.value = UiState.Success("FIDO2 biometric registration successful!")
                }

                override fun onError(exception: Fido2Exception) {
                    val errorCode = exception.error
                    val message = exception.message ?: "Unknown error"
                    Log.e(TAG, "FIDO2 SDK registration error [$errorCode]: $message")

                    fidoRegistrationChallengeHolder.pendingCallback?.returnError(
                        Exception("FIDO2 error [$errorCode]: $message")
                    )
                    fidoRegistrationChallengeHolder.clear()
                    _uiState.value = UiState.Error("FIDO2 error [$errorCode]: $message")
                }
            })
        } catch (e: Fido2Exception) {
            Log.e(TAG, "FIDO2 SDK setup error [${e.error}]: ${e.message}", e)
            fidoRegistrationChallengeHolder.pendingCallback?.returnError(
                Exception("FIDO2 setup error [${e.error}]: ${e.message}")
            )
            fidoRegistrationChallengeHolder.clear()
            _uiState.value = UiState.Error("FIDO2 setup error [${e.error}]: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "FIDO2 SDK exception: ${e.message}", e)
            fidoRegistrationChallengeHolder.pendingCallback?.returnError(e)
            fidoRegistrationChallengeHolder.clear()
            _uiState.value = UiState.Error("Exception: ${e.message}")
        }
    }

    fun selectAuthenticator(authenticator: Fido2AuthenticatorInfo) {
        val state = _uiState.value as? UiState.AuthenticatorSelection ?: return
        val originalIndex = state.allAuthenticators.indexOf(authenticator)
        if (originalIndex < 0) {
            Log.e(TAG, "selectAuthenticator: authenticator '${authenticator.getName()}' not found in original list")
            return
        }
        _uiState.value = UiState.BiometricInProgress
        state.callback.onAuthenticatorSelected(originalIndex)
    }

    fun cancelAuthenticatorSelection() {
        val state = _uiState.value as? UiState.AuthenticatorSelection ?: return
        state.callback.cancel()
        _uiState.value = UiState.Idle
    }

    fun dismiss() {
        fidoRegistrationChallengeHolder.pendingCallback?.returnError(Exception("User cancelled FIDO2 registration"))
        fidoRegistrationChallengeHolder.clear()
        _uiState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
    }
}
//POC-END
