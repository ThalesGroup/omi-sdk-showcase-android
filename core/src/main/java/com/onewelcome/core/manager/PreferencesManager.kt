package com.onewelcome.core.manager

interface PreferencesManager {
  suspend fun isSdkAutoInitializationEnabled(): Boolean
  suspend fun setSdkAutoInitializationEnabled(value: Boolean)
  suspend fun setFirebaseTokenUpdateNeeded(value: Boolean)
  suspend fun isFirebaseTokenUpdateNeeded(): Boolean
}
