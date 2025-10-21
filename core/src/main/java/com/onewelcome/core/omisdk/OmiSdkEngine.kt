package com.onewelcome.core.omisdk

import android.content.Context
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.OneginiClientBuilder
import com.onegini.mobile.sdk.android.handlers.OneginiInitializationHandler
import com.onegini.mobile.sdk.android.handlers.error.OneginiInitializationError
import com.onegini.mobile.sdk.android.model.entity.UserProfile
import com.onewelcome.core.OneginiConfigModel
import com.onewelcome.core.entity.HandlerType
import com.onewelcome.core.omisdk.entity.OmiSdkInitializationSettings
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.core.omisdk.handlers.MobileAuthWithPushRequestHandler
import com.onewelcome.core.omisdk.handlers.PinAuthenticationRequestHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class OmiSdkEngine @Inject constructor(
  @ApplicationContext private val context: Context,
  private val createPinRequestHandler: CreatePinRequestHandler,
  private val pinAuthenticationRequestHandler: PinAuthenticationRequestHandler,
  private val oneginiConfigModel: OneginiConfigModel,
  private val browserRegistrationRequestHandler: BrowserRegistrationRequestHandler,
  private val mobileAuthWithPushRequestHandler: MobileAuthWithPushRequestHandler,
) : OmiSdkFacade {

  //old implementation (I left it to not convert all use cases for this "poc")
  override val oneginiClient
    get() = OneginiClient.instance ?: throw IllegalStateException("Onegini SDK instance not yet initialized")

  private var oneginiClientDeferredResult: CompletableDeferred<OneginiClient>? = null

  //new implementation suspends the coroutine until SDK is started. It will throw exception if initialization wasn't started
  override suspend fun getOneginiClientNew(): OneginiClient {
    return oneginiClientDeferredResult?.await() ?: throw IllegalStateException("Onegini SDK instance not yet initialized")
  }

  override suspend fun initialize(settings: OmiSdkInitializationSettings): Result<Set<UserProfile>, OneginiInitializationError> {
    oneginiClientDeferredResult = CompletableDeferred()
    val oneginiClient = buildClient(settings)
    return startClient(oneginiClient)
      .onSuccess { oneginiClientDeferredResult?.complete(oneginiClient) }
      .onFailure { oneginiClientDeferredResult?.completeExceptionally(it) }
  }

  private fun buildClient(settings: OmiSdkInitializationSettings): OneginiClient {
    return OneginiClientBuilder(context, createPinRequestHandler, pinAuthenticationRequestHandler)
      .setConfigModel(oneginiConfigModel)
      .shouldStoreCookies(settings.shouldStoreCookies)
      .apply {
        settings.httpConnectTimeout?.let { setHttpConnectTimeout(it) }
        settings.httpReadTimeout?.let { setHttpReadTimeout(it) }
        settings.deviceConfigCacheDuration?.let { setDeviceConfigCacheDurationSeconds(it) }
        setOptionalHandlers(settings)
      }.build()
  }

  private fun OneginiClientBuilder.setOptionalHandlers(settings: OmiSdkInitializationSettings) {
    settings.handlers.forEach {
      when (it) {
        HandlerType.BROWSER_REGISTRATION -> setBrowserRegistrationRequestHandler(browserRegistrationRequestHandler)
        HandlerType.MOBILE_AUTH_WITH_PUSH -> setMobileAuthWithPushRequestHandler(mobileAuthWithPushRequestHandler)
      }
    }
  }

  private suspend fun startClient(oneginiClient: OneginiClient): Result<Set<UserProfile>, OneginiInitializationError> {
    return suspendCancellableCoroutine { continuation ->
      oneginiClient.start(object : OneginiInitializationHandler {
        override fun onSuccess(removedUserProfiles: Set<UserProfile>) {
          continuation.resume(Ok(removedUserProfiles))
        }

        override fun onError(error: OneginiInitializationError) {
          continuation.resume(Err(error))
        }
      })
    }
  }
}
