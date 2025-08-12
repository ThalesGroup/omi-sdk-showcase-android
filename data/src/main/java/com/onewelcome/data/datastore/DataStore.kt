package com.onewelcome.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
  private val preferenceFirebaseTokenUpdateKey = booleanPreferencesKey(FIREBASE_TOKEN_UPDATE_KEY)

  fun isFirebaseTokenUpdateNeeded(): Flow<Boolean> {
    return dataStore.data.map { it[preferenceFirebaseTokenUpdateKey] ?: false }
  }

  suspend fun setFirebaseTokenUpdateNeeded(value: Boolean) {
    dataStore.edit { it[preferenceFirebaseTokenUpdateKey] = value }
  }

  companion object PreferenceKeys {
    private const val FIREBASE_TOKEN_UPDATE_KEY = "username"
  }
}
