package com.onewelcome.showcaseapp.core.facade

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onewelcome.showcaseapp.entity.OmiSdkInitializationSettings

interface OmiSdkFacade {

  val oneginiClient: OneginiClient

  fun initialize(settings: OmiSdkInitializationSettings): OneginiClient
}
