package com.onewelcome.core.util

import android.os.Parcel
import com.onegini.mobile.sdk.android.model.OneginiAuthenticator
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.AuthenticationAttemptCounter
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthenticationRequest
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings

object TestConstants {
  val TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS = OmiSdkInitializationSettings(
    shouldStoreCookies = true,
    httpConnectTimeout = null,
    httpReadTimeout = null,
    deviceConfigCacheDuration = null,
    handlers = emptyList(),
  )
  val TEST_USER_PROFILE_1 = UserProfile("123456")
  val TEST_USER_PROFILE_2 = UserProfile("654321")
  val TEST_CUSTOM_INFO = CustomInfo(666, "data")
  val TEST_USER_PROFILES = setOf(TEST_USER_PROFILE_1, TEST_USER_PROFILE_2)
  val TEST_USER_PROFILES_IDS = TEST_USER_PROFILES.map { it.profileId }.toList()
  val TEST_SELECTED_SCOPES = Constants.DEFAULT_SCOPES
  val TEST_AUTHENTICATION_ATTEMPT_COUNTER = AuthenticationAttemptCounter(maxAttempts = 3, failedAttempts = 0)
  val TEST_AUTHENTICATION_ATTEMPT_COUNTER_FAILED_ATTEMPT = AuthenticationAttemptCounter(maxAttempts = 3, failedAttempts = 1)
  val OneginiBrowserIdentityProvider1 = object : OneginiIdentityProvider {
    override val id: String
      get() = "Browser-identity-provider-id-1"
    override val name: String
      get() = "Browser identity provider name 1"

    override fun describeContents(): Int {
      return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
      dest.writeString(id)
      dest.writeString(name)
    }
  }

  val OneginiBrowserIdentityProvider2 = object : OneginiIdentityProvider {
    override val id: String
      get() = "Browser-identity-provider-id-2"
    override val name: String
      get() = "Browser identity provider name 2"

    override fun describeContents(): Int {
      return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
      dest.writeString(id)
      dest.writeString(name)
    }
  }

  val TEST_IDENTITY_PROVIDERS = setOf(OneginiBrowserIdentityProvider1, OneginiBrowserIdentityProvider2)
  val TEST_SELECTED_IDENTITY_PROVIDER = TEST_IDENTITY_PROVIDERS.first()
  val TEST_PIN = charArrayOf('1', '2', '3', '4', '5')
  val TEST_ONEGINI_MOBILE_AUTHENTICATION_REQUEST = OneginiMobileAuthenticationRequest(
    "message", "type", TEST_USER_PROFILE_1, "transactionId", null
  )

  fun getPinAuthenticator() = object : OneginiAuthenticator {
    override val id: String = "pin"
    override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.PIN
    override val name: String = "PIN"
    override val isRegistered: Boolean = true
    override val isPreferred: Boolean = true
    override val userProfile: UserProfile = TEST_USER_PROFILE_1
  }

  fun getBiometricAuthenticator(isRegistered: Boolean) = object : OneginiAuthenticator {
    override val id: String = "biometric"
    override val type: OneginiAuthenticator.Type = OneginiAuthenticator.Type.BIOMETRIC
    override val name: String = "BIOMETRIC"
    override val isRegistered: Boolean = isRegistered
    override val isPreferred: Boolean = false
    override val userProfile: UserProfile = TEST_USER_PROFILE_1
  }
}
