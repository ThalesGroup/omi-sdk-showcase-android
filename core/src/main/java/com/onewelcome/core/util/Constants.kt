package com.onewelcome.core.util

object Constants {
  private const val DOCUMENTATION_BASE_URL = "https://thalesdocs.com/oip/omi-sdk/android-sdk"

  const val DOCUMENTATION_SDK_INITIALIZATION = "$DOCUMENTATION_BASE_URL/android-sdk-getting-started/android-sdk-initialize/index.html"
  const val DOCUMENTATION_USER_REGISTRATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-register-user/index.html"
  const val DOCUMENTATION_USER_DEREGISTRATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-delete-user/index.html"
  const val DOCUMENTATION_PIN_AUTHENTICATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticator-interface/index.html#authenticate-a-user-with-a-pin"
  const val DOCUMENTATION_USER_AUTHENTICATION = "$DOCUMENTATION_BASE_URL/android-sdk-using/android-sdk-authenticating-users/index.html"
  const val OS_COMPATIBILITY_TEST_RESULT_FILE_NAME = "os_compatibility_test_results.txt"
  const val FULLSCREEN_PAGE = "fullscreen"
  val DEFAULT_SCOPES = listOf("read", "openid", "profile", "phone", "email")
}
