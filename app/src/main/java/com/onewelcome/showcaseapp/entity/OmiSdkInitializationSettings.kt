package com.onewelcome.showcaseapp.entity

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?
)
