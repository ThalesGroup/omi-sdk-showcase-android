package com.onewelcome.showcaseapp.fakes

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.core.facade.FirebaseMessagingFacade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMessagingFacadeFake @Inject constructor() : FirebaseMessagingFacade {

  var resultFake: Result<String, Throwable> = Ok("token")

  override suspend fun getToken(): Result<String, Throwable> {
    return resultFake
  }
}
