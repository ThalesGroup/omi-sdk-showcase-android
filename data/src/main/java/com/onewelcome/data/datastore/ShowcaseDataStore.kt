package com.onewelcome.data.datastore

import kotlinx.coroutines.flow.Flow

interface ShowcaseDataStore {

  fun isFirebaseTokenUpdateNeeded(): Flow<Boolean>
  suspend fun setFirebaseTokenUpdateNeeded(value: Boolean)
}
