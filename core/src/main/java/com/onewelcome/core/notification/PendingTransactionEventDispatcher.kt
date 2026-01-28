package com.onewelcome.core.notification

import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingTransactionEventDispatcher @Inject constructor() {

  private val _pendingTransactionEvent = MutableSharedFlow<OneginiMobileAuthWithPushRequest>(extraBufferCapacity = 1)
  val pendingTransactionEvent = _pendingTransactionEvent.asSharedFlow()

  fun dispatch(pushRequest: OneginiMobileAuthWithPushRequest) {
    _pendingTransactionEvent.tryEmit(pushRequest)
  }
}
