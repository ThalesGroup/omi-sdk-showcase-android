package com.onewelcome.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShowcaseDataStoreImpl @Inject constructor(private val dataStore: DataStore<Preferences>) : ShowcaseDataStore {
  private val preferenceFirebaseTokenUpdateKey = booleanPreferencesKey(FIREBASE_TOKEN_UPDATE_KEY)
  private val preferenceSdkAutoInitializationKey = booleanPreferencesKey(SDK_AUTO_INITIALIZATION_KEY)

  override fun isFirebaseTokenUpdateNeeded(): Flow<Boolean> {
    return dataStore.data.map { it[preferenceFirebaseTokenUpdateKey] ?: false }
  }

  override suspend fun setFirebaseTokenUpdateNeeded(value: Boolean) {
    dataStore.edit { it[preferenceFirebaseTokenUpdateKey] = value }
  }

  override fun isSdkAutoInitializationEnabled(): Flow<Boolean> {
    return dataStore.data.map { it[preferenceSdkAutoInitializationKey] ?: true }
  }

  override suspend fun setSdkAutoInitializationEnabled(value: Boolean) {
    dataStore.edit { it[preferenceSdkAutoInitializationKey] = value }
  }

  companion object PreferenceKeys {
    private const val FIREBASE_TOKEN_UPDATE_KEY = "firebase_token_update"
    private const val SDK_AUTO_INITIALIZATION_KEY = "sdk_auto_initialization"
  }
}
