package com.onewelcome.showcaseapp.fcm

import android.os.Bundle
import androidx.core.os.bundleOf
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.facade.JsonFacade
import com.onewelcome.core.manager.PreferencesManager
import com.onewelcome.core.usecase.IsSdkInitializedUseCase
import com.onewelcome.core.usecase.RefreshMobileAuthPushTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowcaseAppFirebaseMessagingService : FirebaseMessagingService() {

  @Inject
  lateinit var isSdkInitializedUseCase: IsSdkInitializedUseCase

  @Inject
  lateinit var refreshMobileAuthPushTokenUseCase: RefreshMobileAuthPushTokenUseCase

  @Inject
  lateinit var jsonFacade: JsonFacade

  @Inject
  lateinit var preferencesManager: PreferencesManager

  @Inject
  lateinit var notificationSender: NotificationSender

  override fun onNewToken(token: String) {
    CoroutineScope(Dispatchers.Default).launch {
      if (isSdkInitializedUseCase.execute()) {
        refreshMobileAuthPushTokenUseCase.execute(token)
      } else {
        preferencesManager.setFirebaseTokenUpdateNeeded(true)
      }
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    parseRemoteMessage(message)
      .onSuccess { notificationSender.showNewTransactionNotification(createBundleForNotification(it)) }
      .onFailure {
        //TODO figure out what should happen
      }
  }

  private fun createBundleForNotification(mobileAuthRequest: OneginiMobileAuthWithPushRequest): Bundle {
    return bundleOf(
      MESSAGE_KEY to mobileAuthRequest.message,
      TRANSACTION_ID_KEY to mobileAuthRequest.transactionId,
      PROFILE_ID_KEY to mobileAuthRequest.userProfileId,
    )
  }

  private fun parseRemoteMessage(remoteMessage: RemoteMessage): Result<OneginiMobileAuthWithPushRequest, Throwable> {
    return remoteMessage.data["content"]?.let { content ->
      if (content.isNotEmpty()) {
        Ok(jsonFacade.fromJson(content, OneginiMobileAuthWithPushRequest::class.java))
      } else {
        Err(IllegalArgumentException("Content of remote message is empty"))
      }
    } ?: Err(IllegalArgumentException("Content of remote message is null"))
  }

  companion object {
    const val MESSAGE_KEY = "message"
    const val TRANSACTION_ID_KEY = "transactionId"
    const val PROFILE_ID_KEY = "profileId"
  }
}
