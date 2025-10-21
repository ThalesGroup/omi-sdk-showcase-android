package com.onewelcome.showcaseapp.fakes

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade

class OmiSdkEngineFake(private val oneginiClientMock: OneginiClient) : OmiSdkFacade {

  var initializationResult: Result<Set<UserProfile>, OneginiInitializationError> = Ok(setOf())
  private var isInitialized: Boolean = false

  override val oneginiClient: OneginiClient
    get() = if (isInitialized) oneginiClientMock else throw IllegalStateException("Onegini SDK instance not yet initialized")

  override suspend fun getOneginiClientNew(): OneginiClient {
    return if (isInitialized) oneginiClientMock else throw IllegalStateException("Onegini SDK instance not yet initialized")
  }

  override suspend fun initialize(settings: OmiSdkInitializationSettings): Result<Set<UserProfile>, OneginiInitializationError> {
    isInitialized = true
    return initializationResult
  }
}
