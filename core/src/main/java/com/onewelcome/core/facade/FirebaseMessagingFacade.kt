package com.onewelcome.core.facade

import com.github.michaelbull.result.Result

interface FirebaseMessagingFacade {

  suspend fun getToken(): Result<String, Throwable>
}
