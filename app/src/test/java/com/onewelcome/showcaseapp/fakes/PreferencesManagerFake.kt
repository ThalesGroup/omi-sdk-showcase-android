package com.onewelcome.showcaseapp.fakes

import com.onewelcome.core.manager.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManagerFake @Inject constructor() : PreferencesManager {
  var isSdkAutoInitializationEnabled = true
  override suspend fun isSdkAutoInitializationEnabled(): Boolean {
    return isSdkAutoInitializationEnabled
  }

  override suspend fun setSdkAutoInitializationEnabled(value: Boolean) {
    isSdkAutoInitializationEnabled = value
  }
}
