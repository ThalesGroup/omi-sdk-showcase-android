package com.onewelcome.core.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.onewelcome.core.facade.FirebaseMessagingFacade
import com.onewelcome.data.datastore.ShowcaseDataStore
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewFirebaseTokenUpdateUseCase @Inject constructor(
  private val dataStore: ShowcaseDataStore,
  private val firebaseMessagingFacade: FirebaseMessagingFacade,
  private val refreshMobileAuthPushTokenUseCase: RefreshMobileAuthPushTokenUseCase
) {

  suspend fun execute(): Result<Unit, Throwable> {
    return if (dataStore.isFirebaseTokenUpdateNeeded().firstOrNull() == true) {
      firebaseMessagingFacade.getToken()
        .flatMap { refreshMobileAuthPushTokenUseCase.execute(it) }
        .map { dataStore.setFirebaseTokenUpdateNeeded(false) }
    } else {
      Ok(Unit)
    }
  }
}
