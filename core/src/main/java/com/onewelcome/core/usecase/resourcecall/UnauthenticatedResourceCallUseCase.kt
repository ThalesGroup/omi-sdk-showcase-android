package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.core.network.RetrofitServiceFactory
import javax.inject.Inject

class UnauthenticatedResourceCallUseCase @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory
) {
    suspend fun getPathResources(): Result<Boolean, Throwable> {
        return try {
            val api = retrofitServiceFactory.createUnauthenticatedApi()
            val response = api.getPathToresource()
            if (response.isSuccessful) {
                Ok(true)
            } else {
                Err(ResourceCallException(
                    code = response.code(),
                    message = "Something went wrong"
                ))
            }
        } catch (e: Exception) {
            Err(e)
        }
    }
}

/**
 * Custom exception for resource call failures
 */
class ResourceCallException(
    val code: Int,
    override val message: String
) : Exception(message)
