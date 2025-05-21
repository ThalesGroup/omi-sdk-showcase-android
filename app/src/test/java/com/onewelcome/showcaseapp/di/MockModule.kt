package com.onewelcome.showcaseapp.di

import com.onegini.mobile.sdk.android.client.OneginiClient
import com.onegini.mobile.sdk.android.client.UserClient
import com.onewelcome.core.omisdk.handlers.BrowserRegistrationRequestHandler
import com.onewelcome.core.omisdk.handlers.CreatePinRequestHandler
import com.onewelcome.showcaseapp.fakes.OmiSdkEngineFake
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MockModule {

  private val oneginiClientMock = mock<OneginiClient>()

  private val userClientMock = mock<UserClient>()

  private val browserRegistrationRequestHandler = mock<BrowserRegistrationRequestHandler>()

  private val createPinRequestHandler = CreatePinRequestHandler()

  @Provides
  fun provideOneginiClientMock(): OneginiClient {
    return oneginiClientMock
  }

  @Provides
  fun provideUserClient(): UserClient {
    return userClientMock
  }

  @Provides
  fun provideBrowserRegistrationRequestHandler(): BrowserRegistrationRequestHandler {
    return browserRegistrationRequestHandler
  }

  @Provides
  fun provideCreatePinRequestHandler(): CreatePinRequestHandler {
    return createPinRequestHandler
  }

  @Provides
  @Singleton
  fun provideOmiSdkEngineFake(): OmiSdkEngineFake {
    return spy(OmiSdkEngineFake(oneginiClientMock))
  }
}
