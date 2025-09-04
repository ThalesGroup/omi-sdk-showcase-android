package com.onewelcome.core.manager

import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PreferencesManagerImpl @Inject constructor(
  private val dataStore: ShowcaseDataStore
) : PreferencesManager {
  override suspend fun isSdkAutoInitializationEnabled(): Boolean {
    return dataStore.isSdkAutoInitializationEnabled().first()
  }

  override suspend fun setSdkAutoInitializationEnabled(value: Boolean) {
    dataStore.setSdkAutoInitializationEnabled(value)
  }

  override suspend fun setFirebaseTokenUpdateNeeded(value: Boolean) {
    dataStore.setFirebaseTokenUpdateNeeded(true)
  }
}
