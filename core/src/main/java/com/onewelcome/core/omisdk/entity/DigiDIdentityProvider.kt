// POC - START
package com.onewelcome.core.omisdk.entity

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider
import com.onewelcome.core.omisdk.handlers.DigiDRegistrationRequestHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigiDIdentityProvider @Inject constructor(
  private val digiDRegistrationRequestHandler: DigiDRegistrationRequestHandler
) : OneginiCustomIdentityProvider {

  companion object {
    const val ID = "poc_digid_two_step"
  }

  override val id: String
    get() = ID
  override val registrationAction: OneginiCustomRegistrationAction
    get() = digiDRegistrationRequestHandler
}
// POC - END
