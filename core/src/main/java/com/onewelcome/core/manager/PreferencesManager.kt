package com.onewelcome.core.manager

interface PreferencesManager {
  suspend fun isSdkAutoInitializationEnabled(): Boolean
  suspend fun setSdkAutoInitializationEnabled(value: Boolean)
}
