package com.onewelcome.showcaseapp.di

import com.onewelcome.core.di.FacadeModule
import com.onewelcome.core.omisdk.facade.OmiSdkFacade
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
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
}
