package com.onewelcome.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
  private val preferenceUsernameKey = stringPreferencesKey(USERNAME_KEY)

  fun getUsername(): Flow<String?> = dataStore.data.map { it[preferenceUsernameKey] }

  suspend fun setUsername(value: String) {
    dataStore.edit { it[preferenceUsernameKey] = value }
  }

  companion object PreferenceKeys {
    private const val USERNAME_KEY = "username"
  }
}
