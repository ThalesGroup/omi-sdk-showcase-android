package com.onewelcome.core.omisdk.entity

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?,
  val deviceConfigCacheDuration: Int?
)
