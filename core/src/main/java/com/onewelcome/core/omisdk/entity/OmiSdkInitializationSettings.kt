package com.onewelcome.core.omisdk.entity

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?,
  val deviceConfigCacheDuration: Int?
) {
  companion object {
    val DEFAULT = OmiSdkInitializationSettings(
      shouldStoreCookies = true,
      httpConnectTimeout = null,
      httpReadTimeout = null,
      deviceConfigCacheDuration = null
    )
  }
}
