package com.onewelcome.core.usecase.resourcecall

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.network.RetrofitServiceFactory
import com.onewelcome.core.network.api.DecoratedIdModel
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import javax.inject.Inject



class ImplicitResourceCallUseCase @Inject constructor(
  private val retrofitServiceFactory: RetrofitServiceFactory,
  private val omiSdkFacade: OmiSdkFacade
) {
  fun getImplicitlyAuthenticatedUserProfile(): UserProfile? {
    return omiSdkFacade.oneginiClient.getUserClient().implicitlyAuthenticatedUserProfile
  }

  suspend fun getUserId(): Result<DecoratedIdModel, Throwable> {
    return try {
      val api = retrofitServiceFactory.createImplicitApi()
      val response = api.getDecoratedUserId()
      if (response.isSuccessful && response.body() != null) {
        Ok(response.body()!!)
      } else if (response.code() == 401) {
        // Implicit token expired or invalid, need to re-authenticate
        Err(ImplicitTokenExpiredException("Implicit token expired, call authenticateUserImplicitly() again"))
      } else {
        Err(
          ResourceCallException(
            code = response.code(),
            message = response.message() ?: "Failed to fetch user basic profile"
          )
        )
      }
    } catch (e: Exception) {
      Err(e)
    }
  }
}

/**
 * Exception thrown when implicit token is expired
 */
class ImplicitTokenExpiredException(
  override val message: String
) : Exception(message)
