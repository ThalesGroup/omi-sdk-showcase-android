package com.onewelcome.showcaseapp.di

import com.onewelcome.showcaseapp.fcm.NotificationFacade
import com.onewelcome.showcaseapp.fcm.NotificationFacadeImpl
import com.onewelcome.showcaseapp.fcm.PendingIntentFacade
import com.onewelcome.showcaseapp.fcm.PendingIntentFacadeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FacadeModule {
  @Binds
  fun bindNotificationFacade(notificationFacadeImpl: NotificationFacadeImpl): NotificationFacade

  @Binds
  fun bindPendingIntentFacade(pendingIntentFacadeImpl: PendingIntentFacadeImpl): PendingIntentFacade
}
