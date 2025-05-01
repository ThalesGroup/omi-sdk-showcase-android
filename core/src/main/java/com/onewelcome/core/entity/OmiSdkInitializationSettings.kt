package com.onewelcome.core.entity

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?,
  val deviceConfigCacheDuration: Int?
)
