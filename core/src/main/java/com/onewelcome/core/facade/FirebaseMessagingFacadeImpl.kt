package com.onewelcome.core.facade

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseMessagingFacadeImpl @Inject constructor() : FirebaseMessagingFacade {

  private val instance = FirebaseMessaging.getInstance()

  override suspend fun getToken(): Result<String, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      instance.token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
          continuation.resume(Ok(task.result))
        } else {
          val error = task.exception ?: Exception("Failed to fetch Firebase Messaging token")
          continuation.resume(Err(error))
        }
      }
    }
  }
}
