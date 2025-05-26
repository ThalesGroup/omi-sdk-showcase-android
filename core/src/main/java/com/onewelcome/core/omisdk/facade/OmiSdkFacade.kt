package com.onewelcome.core.omisdk.facade

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings

interface OmiSdkFacade {

  val oneginiClient: OneginiClient

  fun initialize(settings: OmiSdkInitializationSettings): OneginiClient
}
