package com.onewelcome.showcaseapp.omisdk

import android.content.Context
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val pinRequestHandler: PinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
) {
  val omiSdk
    get() = OneginiClient.instance ?: init()

  private fun init(): OneginiClient {
    return OneginiClientBuilder(context, pinRequestHandler, pinAuthenticationRequestHandler)
      .build()
  }
}
