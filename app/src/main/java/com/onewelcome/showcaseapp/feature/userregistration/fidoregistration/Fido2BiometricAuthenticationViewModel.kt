//POC-START
package com.onewelcome.showcaseapp.feature.userregistration.fidoregistration

import android.app.Activity
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import com.onewelcome.core.omisdk.handlers.FidoAuthenticationChallengeHolder
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.authenticator.biometric.BiometricAuthenticatorCallback
import com.thalesgroup.gemalto.fido2.client.AuthenticatorDescriptionCallback
import com.thalesgroup.gemalto.fido2.client.AuthenticatorSelectionCallback
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorInfo
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.Fido2Config
import com.thalesgroup.gemalto.fido2.client.Fido2Request
import com.thalesgroup.gemalto.fido2.client.Fido2RespondArgs
import com.thalesgroup.gemalto.fido2.client.Fido2Response
import com.thalesgroup.gemalto.fido2.client.Fido2ResponseCallback
import com.thalesgroup.gemalto.fido2.client.Fido2UiCallback
import com.thalesgroup.gemalto.fido2.client.TransactionDetailsCallback
import com.thalesgroup.gemalto.fido2.client.VerifyMethod
import com.thalesgroup.gemalto.securelog.SecureLogConfig
import com.thalesgroup.gemalto.securelog.SecureLogLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "Fido2AuthBiometricVM"

/**
 * ViewModel for the FIDO2 biometric authentication screen.
 *
 * Flow:
 * 1. OMI SDK calls initRegistration → sends empty string to /init
 * 2. OMI SDK calls finishRegistration → FIDO2 challenge JSON stored in [FidoAuthenticationChallengeHolder]
 * 3. FidoRegistrationViewModel navigates to this screen
 * 4. This VM calls Thales FIDO2 SDK with the challenge JSON → biometric prompt shown
 * 5. Thales FIDO2 SDK returns response JSON
 *    → Wrapped as plain JSON object: {"fido2": {<responseObject>}, "userId": "<userId>"}
 *    → Call stored OMI SDK callback with payload → OMI SDK sends to server /complete API
 * 6. Screen closes automatically → returns to FidoRegistration screen
 */
@HiltViewModel
class Fido2BiometricAuthenticationViewModel @Inject constructor(
    private val fidoAuthenticationChallengeHolder: FidoAuthenticationChallengeHolder,
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object BiometricInProgress : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val pendingChallengeJson = fidoAuthenticationChallengeHolder.pendingChallengeJson

    /**
     * Extracts the inner FIDO2 PublicKeyCredentialRequestOptions JSON from the server response.
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
                    val cleanRpId =
                        rpId.removePrefix("https://").removePrefix("http://").trimEnd('/')
                    if (cleanRpId != rpId) {
                        rp.put("id", cleanRpId)
                    }
                }
            }

            if (fido2Object.has("rpId")) {
                val rpId = fido2Object.getString("rpId")
                val cleanRpId = rpId.removePrefix("https://").removePrefix("http://").trimEnd('/')
                if (cleanRpId != rpId) {
                    fido2Object.put("rpId", cleanRpId)
                }
            }

            Pair(fido2Object.toString(), userId)
        } catch (e: JSONException) {
            Log.w(TAG, "Could not parse wrapper JSON, using raw JSON: ${e.message}")
            Pair(wrappedJson, null)
        }
    }

    /**
     * Starts the Thales FIDO2 SDK authentication using the stored challenge JSON.
     * On success, calls the stored OMI SDK callback with the FIDO2 response + userId,
     * then transitions to Success state so the screen auto-closes.
     *
     * @param activity Required by Thales FIDO2 SDK to show the biometric/PIN prompt.
     * @param challengeJson The wrapped server JSON containing the FIDO2 challenge.
     */
    fun startFido2Authentication(activity: Activity, challengeJson: String) {
        Log.d(TAG, "=== AUTHENTICATION: INIT API RESPONSE (challenge received) ===")
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
                        val selectable =
                            authenticators.filterNot { it.verifyMethod == VerifyMethod.PLATFORM }
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
                            val selectedIndex = authenticators.indexOf(selectable[0])
                            _uiState.value = UiState.BiometricInProgress
                            callback.onAuthenticatorSelected(selectedIndex)
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
                    "Verify your identity to authenticate"
                })
                .build()

            try {
                val dummyModulus = ByteArray(256) { 0x01 }
                val dummyExponent = byteArrayOf(0x01, 0x00, 0x01)
                val secureLogConfig = SecureLogConfig.Builder(activity)
                    .fileID("fido2-auth")
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

                    Log.d(TAG, "Payload: $payload")

                    val callback = fidoAuthenticationChallengeHolder.pendingCallback
                    if (callback != null) {
                        callback.returnSuccess(payload)
                    } else {
                               _uiState.value =
                            UiState.Error("Internal error: OMI SDK authentication callback lost")
                        fidoAuthenticationChallengeHolder.clear()
                        return
                    }

                    fidoAuthenticationChallengeHolder.clear()
                    // Transition to Success — screen will auto-close via LaunchedEffect
                    _uiState.value = UiState.Success("FIDO2 biometric authentication successful!")
                }

                override fun onError(exception: Fido2Exception) {
                    val errorCode = exception.error
                    val message = exception.message ?: "Unknown error"
                    Log.e(TAG, "FIDO2 SDK authentication error [$errorCode]: $message")

                    fidoAuthenticationChallengeHolder.pendingCallback?.returnError(
                        Exception("FIDO2 auth error [$errorCode]: $message")
                    )
                    fidoAuthenticationChallengeHolder.clear()
                    _uiState.value = UiState.Error("FIDO2 auth error [$errorCode]: $message")
                }
            })
        } catch (e: Fido2Exception) {
            fidoAuthenticationChallengeHolder.pendingCallback?.returnError(
                Exception("FIDO2 auth setup error [${e.error}]: ${e.message}")
            )
            fidoAuthenticationChallengeHolder.clear()
            _uiState.value = UiState.Error("FIDO2 auth setup error [${e.error}]: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "FIDO2 SDK exception: ${e.message}", e)
            fidoAuthenticationChallengeHolder.pendingCallback?.returnError(e)
            fidoAuthenticationChallengeHolder.clear()
            _uiState.value = UiState.Error("Exception: ${e.message}")
        }
    }

    fun dismiss() {
        fidoAuthenticationChallengeHolder.pendingCallback?.returnError(Exception("User cancelled FIDO2 authentication"))
        fidoAuthenticationChallengeHolder.clear()
        _uiState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
    }
}
//POC-END
