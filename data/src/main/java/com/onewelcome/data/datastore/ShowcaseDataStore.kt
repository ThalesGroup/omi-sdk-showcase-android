package com.onewelcome.data.datastore

import kotlinx.coroutines.flow.Flow

interface ShowcaseDataStore {

  fun isFirebaseTokenUpdateNeeded(): Flow<Boolean>
  suspend fun setFirebaseTokenUpdateNeeded(value: Boolean)
  fun isSdkAutoInitializationEnabled(): Flow<Boolean>
  suspend fun setSdkAutoInitializationEnabled(value: Boolean)

//POC-START

  /** Returns the persisted FIDO2 userId, or null if not yet registered. */
  fun getFidoUserId(): Flow<String?>

  /** Persists the FIDO2 userId. Pass null to clear it. */
  suspend fun setFidoUserId(userId: String?)

//POC-END
}
