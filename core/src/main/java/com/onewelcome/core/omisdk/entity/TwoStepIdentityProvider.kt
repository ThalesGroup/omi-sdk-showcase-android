package com.onewelcome.core.omisdk.entity

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider
import com.onewelcome.core.omisdk.handlers.TwoStepRegistrationRequestHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwoStepIdentityProvider @Inject constructor(
  private val twoStepRegistrationRequestHandler: TwoStepRegistrationRequestHandler
) : OneginiCustomIdentityProvider {

  companion object {
    const val ID = "New2step"
  }

  override val id: String
    get() = ID
  override val registrationAction: OneginiCustomRegistrationAction
    get() = twoStepRegistrationRequestHandler
}
