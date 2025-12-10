package com.onewelcome.core.omisdk.identityproviders

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider
import com.onewelcome.core.omisdk.actions.QrCodeRegistrationAction
import javax.inject.Inject

class QrCodeIdentityProvider @Inject constructor(private val qrCodeRegistrationAction: QrCodeRegistrationAction) :
  OneginiCustomIdentityProvider {
  override val id: String
    get() = "qr_registration"
  override val registrationAction: OneginiCustomRegistrationAction
    get() = qrCodeRegistrationAction
}
