package com.onewelcome.core.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class GetFirebaseRegistrationTokenUseCase @Inject constructor() {

  suspend fun execute(): Result<String, Throwable> {
    return suspendCancellableCoroutine { continuation ->
      FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
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
