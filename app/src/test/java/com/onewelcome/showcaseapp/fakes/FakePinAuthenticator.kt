package com.onewelcome.showcaseapp.fakes

import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.util.TestConstants.TEST_USER_PROFILE_1

class FakePinAuthenticator : OneginiAuthenticator {
  override val id: String
    get() = "fakePinAuthenticatorId"
  override val type: OneginiAuthenticator.Type
    get() = OneginiAuthenticator.Type.PIN
  override val name: String
    get() = "fakePinAuthenticatorName"
  override val isRegistered: Boolean
    get() = true
  override val isPreferred: Boolean
    get() = false
  override val userProfile: UserProfile
    get() = TEST_USER_PROFILE_1

}