package com.onewelcome.core.util

object Constants {
  private const val DOCUMENTATION_BASE_URL = "https://thalesdocs.com/oip/omi-sdk/android-sdk"

  const val DOCUMENTATION_SDK_INITIALIZATION = "$DOCUMENTATION_BASE_URL/android-sdk-getting-started/android-sdk-initialize/index.html"
  const val DOCUMENTATION_USER_REGISTRATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-register-user/index.html"
  const val DOCUMENTATION_USER_DEREGISTRATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-delete-user/index.html"
  const val DOCUMENTATION_PIN_AUTHENTICATION =
    "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticator-interface/index.html#authenticate-a-user-with-a-pin"
  const val DOCUMENTATION_USER_AUTHENTICATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticating-users/index.html"
  const val DOCUMENTATION_MOBILE_AUTHENTICATION =
    "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticating-users/android-sdk-mobile-authentication/index.html#enrollment"
  const val DOCUMENTATION_MOBILE_AUTHENTICATION_WITH_PUSH =
    "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticating-users/android-sdk-mobile-authentication/index.html#mobile-authentication-with-push"
  const val DOCUMENTATION_CHANGE_PIN =
    "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-change-pin/index.html"
  const val DOCUMENTATION_LOGOUT =
    "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-logout-user/index.html"
  const val OS_COMPATIBILITY_TEST_RESULT_FILE_NAME = "os_compatibility_test_results.txt"
  const val FULLSCREEN_PAGE = "fullscreen"
  val DEFAULT_SCOPES = listOf("read", "openid", "profile", "phone", "email")

  // OneginiMobileAuthWithPushRequest
  const val MOBILE_AUTH_CHANNEL_ID = "mobile_auth_channel"
  const val MESSAGE_KEY = "message"
  const val TRANSACTION_ID_KEY = "transactionId"
  const val PROFILE_ID_KEY = "profileId"
  const val TIMESTAMP_KEY ="timestamp"
  const val TIME_TO_LIVE_SECONDS_KEY ="timeToLiveSeconds"
}
