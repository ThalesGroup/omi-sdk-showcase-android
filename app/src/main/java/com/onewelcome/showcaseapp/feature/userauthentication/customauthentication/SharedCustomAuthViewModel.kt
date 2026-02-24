package com.onewelcome.showcaseapp.feature.userauthentication.customauthentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onewelcome.core.omisdk.handlers.CustomAuthAuthenticationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthDeregistrationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthRegistrationAction
import com.onewelcome.core.omisdk.handlers.CustomAuthenticationRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushCustomRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel that listens to custom authentication registration and authentication requests.
 * This ViewModel is used at the root navigation level to intercept custom auth events
 * and navigate to the password input screen.
 */
@HiltViewModel
class SharedCustomAuthViewModel @Inject constructor(
  private val customAuthRegistrationAction: CustomAuthRegistrationAction,
  private val customAuthAuthenticationAction: CustomAuthAuthenticationAction,
  private val customAuthenticationRequestHandler: CustomAuthenticationRequestHandler,
  private val customAuthDeregistrationAction: CustomAuthDeregistrationAction,
  private val mobileAuthWithPushCustomRequestHandler: MobileAuthWithPushCustomRequestHandler
) : ViewModel() {

  private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
  val navigationEvents = _navigationEvents.receiveAsFlow()

  init {
    viewModelScope.launch {
      launch {
        customAuthDeregistrationAction.deregistrationRequestFlow.collect {
          customAuthDeregistrationAction.acceptDeregistration()
        }
      }
      launch {
        // Listen for registration requests (when user enables custom authenticator)
        customAuthRegistrationAction.registrationRequestFlow.collect {

          _navigationEvents.trySend(NavigationEvent.NavigateToCustomAuthPasswordScreen(isRegistration = true))
        }
      }
      launch {
        // Listen for authentication requests via CustomAuthenticationRequestHandler
        customAuthenticationRequestHandler.startAuthenticationFlow.collect {
           if (mobileAuthWithPushCustomRequestHandler.currentRequest == null) {
             customAuthenticationRequestHandler.acceptAuthenticationRequest()
             _navigationEvents.trySend(NavigationEvent.NavigateToCustomAuthPasswordScreen(isRegistration = false))
           }
        }
      }
    }
  }

  sealed interface NavigationEvent {
    data class NavigateToCustomAuthPasswordScreen(val isRegistration: Boolean) : NavigationEvent
  }
}
