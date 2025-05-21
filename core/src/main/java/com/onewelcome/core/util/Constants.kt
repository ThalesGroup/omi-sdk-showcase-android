package com.onewelcome.core.util

import android.os.Parcel
import com.onegini.mobile.sdk.android.model.OneginiIdentityProvider
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings

object Constants {
  const val DOCUMENTATION_SDK_INITIALIZATION =
    "https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-getting-started/android-sdk-initialize/index.html"
  const val DOCUMENTATION_USER_REGISTRATION =
    "https://thalesdocs.com/oip/omi-sdk/android-sdk/android-sdk-using/android-sdk-register-user/index.html"
  const val OS_COMPATIBILITY_TEST_RESULT_FILE_NAME = "os_compatibility_test_results.txt"
  val DEFAULT_SCOPES = listOf("read", "openid", "profile", "phone", "email")

  // Test constants
  val TEST_DEFAULT_SDK_INITIALIZATION_SETTINGS = OmiSdkInitializationSettings(
    shouldStoreCookies = true,
    httpConnectTimeout = null,
    httpReadTimeout = null,
    deviceConfigCacheDuration = null
  )
  val TEST_USER_PROFILE_1 = UserProfile("123456")
  val TEST_USER_PROFILE_2 = UserProfile("654321")
  val TEST_CUSTOM_INFO = CustomInfo(666, "data")
  val TEST_USER_PROFILES = setOf(TEST_USER_PROFILE_1, TEST_USER_PROFILE_2)
  val TEST_USER_PROFILES_IDS = TEST_USER_PROFILES.map { it.profileId }.toList()
  val TEST_SELECTED_SCOPES = DEFAULT_SCOPES
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
}
