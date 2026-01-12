package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.OneginiDeviceAuthenticationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiDeviceAuthenticationError
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.ApplicationDetails
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AnonymousResourceCallUseCase @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory,
    private val omiSdkFacade: OmiSdkFacade
) {
    suspend fun authenticateDevice(scopes: Array<String?>? = emptyArray()): Result<Unit, Throwable> {
        return suspendCancellableCoroutine { continuation ->
            omiSdkFacade.oneginiClient.getDeviceClient().authenticateDevice(
                scopes,
                object : OneginiDeviceAuthenticationHandler {
                    override fun onSuccess() {
                        // Device is now authenticated, token stored internally by SDK
                        continuation.resume(Ok(Unit))
                    }

                    override fun onError(error: OneginiDeviceAuthenticationError) {
                        when (error.errorType) {
                            OneginiDeviceAuthenticationError.Type.DEVICE_DEREGISTERED -> {
                                continuation.resume(
                                    Err(
                                        DeviceDeregisteredException(
                                            error.message
                                        )
                                    )
                                )

                            }

                            else -> {
                                continuation.resume(
                                    Err(
                                        DeviceAuthenticationException(
                                            message = error.message
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    suspend fun getApplicationDetails(): Result<ApplicationDetails, Throwable> {
        return try {
            val api = retrofitServiceFactory.createAnonymousApi()
            val response = api.getApplicationDetails()
            if (response.isSuccessful && response.body() != null) {
                Ok(response.body()!!)
            } else if (response.code() == 401) {
                // Device token expired or invalid, need to re-authenticate
                Err(DeviceTokenExpiredException("Device token expired, call authenticateDevice() again"))
            } else {
                Err(
                    ResourceCallException(
                        code = response.code(),
                        message = response.message() ?: "Failed to fetch application details"
                    )
                )
            }
        } catch (e: Exception) {
            Err(e)
        }
    }
    suspend fun authenticateAndGetAppDetails(scopes: Array<String?> = emptyArray()): Result<ApplicationDetails, Throwable> {
        // Step 1: Authenticate device
        val authResult = authenticateDevice(scopes)

        if (authResult is OneginiDeviceAuthenticationError)
            return authResult as Result<ApplicationDetails, Throwable>
        // Step 2: Fetch app details
        return getApplicationDetails()
    }
}

/**
 * Exception thrown when device authentication fails
 */
class DeviceAuthenticationException(
    override val message: String
) : Exception(message)

/**
 * Exception thrown when device is deregistered
 */
class DeviceDeregisteredException(
    override val message: String
) : Exception(message)

/**
 * Exception thrown when device token is expired
 */
class DeviceTokenExpiredException(
    override val message: String
) : Exception(message)
