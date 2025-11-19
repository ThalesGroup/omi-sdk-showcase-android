package com.onewelcome.showcaseapp.di

import com.onewelcome.core.di.FacadeModule
import com.onewelcome.core.facade.BiometricFacade
import com.onewelcome.core.facade.FirebaseMessagingFacade
import com.onewelcome.core.facade.JsonFacade
import com.onewelcome.core.facade.PermissionsFacade
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.showcaseapp.fakes.BiometricFacadeFake
import com.onewelcome.showcaseapp.fakes.FirebaseMessagingFacadeFake
import com.onewelcome.showcaseapp.fakes.JsonFacadeFake
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import com.onewelcome.showcaseapp.fakes.PermissionsFacadeFake
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [FacadeModule::class]
)
interface FakeFacadeModule {

  @Binds
  fun bindFakeOmiSdkEngine(omiSdkEngineFake: OmiSdkEngineFake): OmiSdkFacade

  @Binds
  fun bindFirebaseMessagingFacadeFake(firebaseMessagingFacadeFake: FirebaseMessagingFacadeFake): FirebaseMessagingFacade

  @Binds
  fun bindPermissionsFacadeFake(permissionsFacadeFake: PermissionsFacadeFake): PermissionsFacade

  @Binds
  fun bindJsonFacadeFake(jsonFacadeFake: JsonFacadeFake): JsonFacade

  @Binds
  fun bindBiometricFacade(biometricFacade: BiometricFacadeFake): BiometricFacade
}
