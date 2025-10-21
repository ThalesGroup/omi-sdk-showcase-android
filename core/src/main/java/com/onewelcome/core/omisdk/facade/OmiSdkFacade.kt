package com.onewelcome.core.omisdk.facade

import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings

interface OmiSdkFacade {

  val oneginiClient: OneginiClient
  suspend fun getOneginiClientNew(): OneginiClient

  suspend fun initialize(settings: OmiSdkInitializationSettings): Result<Set<UserProfile>, OneginiInitializationError>
}
