package com.onewelcome.showcaseapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PushNavigationViewModel @Inject constructor() : ViewModel() {
  private val _pushEvent = Channel<OneginiMobileAuthWithPushRequest>(Channel.BUFFERED)
  val pushEvent = _pushEvent.receiveAsFlow()

  fun onNewPush(push: OneginiMobileAuthWithPushRequest) {
    viewModelScope.launch {
      _pushEvent.send(push)
    }
  }
}
