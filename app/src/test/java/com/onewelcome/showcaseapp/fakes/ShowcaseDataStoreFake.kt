package com.onewelcome.showcaseapp.fakes

import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowcaseDataStoreFake @Inject constructor() : ShowcaseDataStore {

  var isFirebaseTokenUpdateNeeded: Boolean = false

  override fun isFirebaseTokenUpdateNeeded(): Flow<Boolean> {
    return flowOf(isFirebaseTokenUpdateNeeded)
  }

  override suspend fun setFirebaseTokenUpdateNeeded(value: Boolean) {
    isFirebaseTokenUpdateNeeded = value
  }
}
