package com.onewelcome.showcaseapp.fakes

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onewelcome.showcaseapp.core.facade.OmiSdkFacade
import com.onewelcome.showcaseapp.entity.OmiSdkInitializationSettings

class OmiSdkEngineFake(private val oneginiClientMock: OneginiClient) : OmiSdkFacade {

  private var isInitialized: Boolean = false

  override val oneginiClient: OneginiClient
    get() = if (isInitialized) oneginiClientMock else throw IllegalStateException("Onegini SDK instance not yet built")

  override fun initialize(settings: OmiSdkInitializationSettings): OneginiClient {
    isInitialized = true
    return oneginiClientMock
  }
}
