package com.onewelcome.core.omisdk.entity

import com.onewelcome.core.entity.HandlerType

data class OmiSdkInitializationSettings(
  val shouldStoreCookies: Boolean,
  val httpConnectTimeout: Int?,
  val httpReadTimeout: Int?,
  val deviceConfigCacheDuration: Int?,
  val handlers: List<HandlerType>,
)
