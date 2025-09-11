package com.onewelcome.showcaseapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PushViewModel @Inject constructor(
  private val authenticateWithPushUseCase: AuthenticateWithPushUseCase,
) : ViewModel() {
  private val _pushEvent = Channel<OneginiMobileAuthWithPushRequest>(Channel.BUFFERED)
  val pushEvent = _pushEvent.receiveAsFlow()

  fun onNewPush(pushRequest: OneginiMobileAuthWithPushRequest) {
    viewModelScope.launch {
      _pushEvent.send(pushRequest)
      authenticateWithPushUseCase.execute(pushRequest)
    }
  }
}
