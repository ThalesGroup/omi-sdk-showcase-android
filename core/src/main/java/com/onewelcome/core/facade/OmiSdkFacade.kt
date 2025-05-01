package com.onewelcome.core.facade

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onewelcome.core.entity.OmiSdkInitializationSettings

interface OmiSdkFacade {

  val oneginiClient: OneginiClient

  fun initialize(settings: OmiSdkInitializationSettings): OneginiClient
}
