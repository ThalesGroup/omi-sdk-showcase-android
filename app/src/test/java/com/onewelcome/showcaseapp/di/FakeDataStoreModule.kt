package com.onewelcome.showcaseapp.di

import com.onewelcome.data.datastore.ShowcaseDataStore
import com.onewelcome.data.di.DataStoreModule
import com.onewelcome.showcaseapp.fakes.ShowcaseDataStoreFake
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [DataStoreModule::class]
)
class FakeDataStoreModule {

  @Provides
  @Singleton
  fun bindShowcaseDataStore(fakeShowcaseDataStore: ShowcaseDataStoreFake): ShowcaseDataStore {
    return fakeShowcaseDataStore
  }
}
