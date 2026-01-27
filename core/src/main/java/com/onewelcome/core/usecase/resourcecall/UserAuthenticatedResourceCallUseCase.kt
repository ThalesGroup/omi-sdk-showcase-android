package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.Devices

import javax.inject.Inject

class UserAuthenticatedResourceCallUseCase @Inject constructor(
  private val retrofitServiceFactory: RetrofitServiceFactory
) {
  suspend fun getDeviceList(): Result<Devices, Throwable> {
    return try {
      val api = retrofitServiceFactory.createUserAuthenticatedApi()
      val response = api.getDevices()
      if (response.isSuccessful && response.body() != null) {
        Ok(response.body()!!)
      } else if (response.code() == 401) {
        // User token expired and could not be refreshed
        Err(UserAuthenticationRequiredException("User session expired, please log in again"))
      } else {
        Err(
          ResourceCallException(
            code = response.code(),
            message = response.message() ?: "Failed to fetch user profile"
          )
        )
      }
    } catch (e: Exception) {
      Err(e)
    }
  }
}

class UserAuthenticationRequiredException(
  override val message: String
) : Exception(message)
