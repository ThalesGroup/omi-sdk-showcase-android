//POC-START
package com.onewelcome.core.omisdk.entity

import com.onegini.mobile.sdk.android.handlers.action.OneginiCustomRegistrationAction
import com.onegini.mobile.sdk.android.model.OneginiCustomIdentityProvider
import com.onewelcome.core.omisdk.handlers.FidoAuthenticationRequestHandler
import com.onewelcome.core.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom identity provider for the FIDO2 authentication flow.
 *
 * This IDP is registered for [Constants.IDP_FIDO_AUTH] ("poc_fido_auth_2_step").
 * It uses [FidoAuthenticationRequestHandler] which sends the stored userId in
 * initRegistration and handles the FIDO2 challenge in finishRegistration.
 */
@Singleton
class FidoAuthenticationIdentityProvider @Inject constructor(
    private val fidoAuthenticationRequestHandler: FidoAuthenticationRequestHandler
) : OneginiCustomIdentityProvider {

    override val id: String
        get() = Constants.IDP_FIDO_AUTH

    override val registrationAction: OneginiCustomRegistrationAction
        get() = fidoAuthenticationRequestHandler
}
//POC-END
