package com.onewelcome.showcaseapp.fakes

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import org.robolectric.shadows.ShadowSystemProperties.override

class OmiSdkEngineFake(private val oneginiClientMock: OneginiClient) : OmiSdkFacade {

  private var isInitialized: Boolean = false

  override val oneginiClient: OneginiClient
    get() = if (isInitialized) oneginiClientMock else throw IllegalStateException("Onegini SDK instance not yet initialized")


  override fun initialize(settings: OmiSdkInitializationSettings): OneginiClient {
    isInitialized = true
    return oneginiClientMock
  }
}
