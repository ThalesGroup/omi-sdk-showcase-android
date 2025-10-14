package com.onewelcome.core.di

import com.onewelcome.core.facade.SdkAutoInitializationFacade
import com.onewelcome.core.facade.SdkAutoInitializationFacadeImpl
import com.onewelcome.core.facade.FirebaseMessagingFacade
import com.onewelcome.core.facade.FirebaseMessagingFacadeImpl
import com.onewelcome.core.facade.JsonFacade
import com.onewelcome.core.facade.JsonFacadeImpl
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.facade.PermissionsFacadeImpl
import com.onewelcome.core.omisdk.OmiSdkEngine
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FacadeModule {

  @Binds
  fun bindOmiSdkFacade(omiSdkEngine: OmiSdkEngine): OmiSdkFacade

  @Binds
  fun bindFirebaseMessagingFacade(firebaseMessagingFacade: FirebaseMessagingFacadeImpl): FirebaseMessagingFacade

  @Binds
  fun bindPermissionsFacade(contextCompatFacadeIml: PermissionsFacadeImpl): PermissionsFacade

  @Binds
  fun bindJsonFacade(jsonFacade: JsonFacadeImpl): JsonFacade

  @Binds
  fun bindAutoInitializationFacade(autoInitializationFacadeImpl: SdkAutoInitializationFacadeImpl): SdkAutoInitializationFacade
}
