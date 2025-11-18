package com.onewelcome.core.di

import com.onewelcome.core.facade.BiometricFacade
import com.onewelcome.core.facade.BiometricFacadeImpl
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FacadeModule {

  @Binds
  @Singleton
  fun bindOmiSdkFacade(omiSdkEngine: OmiSdkEngine): OmiSdkFacade

  @Binds
  @Singleton
  fun bindFirebaseMessagingFacade(firebaseMessagingFacade: FirebaseMessagingFacadeImpl): FirebaseMessagingFacade

  @Binds
  @Singleton
  fun bindPermissionsFacade(contextCompatFacadeIml: PermissionsFacadeImpl): PermissionsFacade

  @Binds
  @Singleton
  fun bindJsonFacade(jsonFacade: JsonFacadeImpl): JsonFacade

  @Binds
  @Singleton
  fun bindBiometricFacade(biometricFacade: BiometricFacadeImpl): BiometricFacade
}
