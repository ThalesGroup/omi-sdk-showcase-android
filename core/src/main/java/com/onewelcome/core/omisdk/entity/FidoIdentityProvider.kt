//POC-START
package com.onewelcome.core.omisdk.entity

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider
import com.onewelcome.core.omisdk.handlers.FidoRegistrationRequestHandler
import com.onewelcome.core.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FidoIdentityProvider @Inject constructor(
    private val fidoRegistrationRequestHandler: FidoRegistrationRequestHandler
) : OneginiCustomIdentityProvider {

    override val id: String
        get() = Constants.IDP_FIDO_CHECK

    override val registrationAction: OneginiCustomRegistrationAction
        get() = fidoRegistrationRequestHandler
}
//POC-END
