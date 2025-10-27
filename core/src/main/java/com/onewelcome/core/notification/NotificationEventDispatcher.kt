package com.onewelcome.core.notification

import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiMobileAuthenticationError
import com.onegini.mobile.sdk.android.model.entity.CustomInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationEventDispatcher @Inject constructor() {
  private val _authenticationEvent = Channel<Result<CustomInfo?, OneginiMobileAuthenticationError>>(Channel.BUFFERED)
  val authenticationEvent = _authenticationEvent.receiveAsFlow()

  fun send(event: Result<CustomInfo?, OneginiMobileAuthenticationError>) {
    _authenticationEvent.trySend(event)
  }
}
