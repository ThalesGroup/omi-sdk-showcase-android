package com.onewelcome.core.omisdk.entity

import com.onegini.mobile.sdk.android.model.OneginiCustomAuthenticator
import com.onewelcome.core.omisdk.handlers.CustomAuthAuthenticationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthDeregistrationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthRegistrationAction


class CustomAuthenticator(
  override val registrationAction: CustomAuthRegistrationAction,
  override val deregistrationAction: CustomAuthDeregistrationAction,
  override val authenticationAction: CustomAuthAuthenticationAction
) : OneginiCustomAuthenticator {

  companion object {
    const val CUSTOM_AUTHENTICATOR_ID = "EXPERIMENTAL_CA_ID"
  }

  override val id: String = CUSTOM_AUTHENTICATOR_ID
}
