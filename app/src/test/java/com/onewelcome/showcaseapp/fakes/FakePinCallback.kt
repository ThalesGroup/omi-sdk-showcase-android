package com.onewelcome.showcaseapp.fakes

import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback

class FakePinCallback : OneginiPinCallback {
    override fun acceptAuthenticationRequest(pin: CharArray) {
      //no-op
    }

    override fun denyAuthenticationRequest() {
      //no-op
    }
  }