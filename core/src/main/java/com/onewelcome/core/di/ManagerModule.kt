package com.onewelcome.core.di

import com.onewelcome.core.manager.PreferencesManager
import com.onewelcome.core.manager.PreferencesManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ManagerModule {

  @Binds
  fun bindPreferencesManager(preferencesManager: PreferencesManagerImpl): PreferencesManager
}
