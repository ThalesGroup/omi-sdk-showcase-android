package com.onewelcome.showcaseapp

import android.app.Application
import com.onewelcome.core.usecase.SdkAutoInitializationUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ShowcaseApplication : Application() {

  @Inject
  lateinit var sdkAutoInitializationUseCase: SdkAutoInitializationUseCase

  override fun onCreate() {
    super.onCreate()
    autoInitializeSdk()
  }

  private fun autoInitializeSdk() {
    CoroutineScope(Dispatchers.Default).launch {
      sdkAutoInitializationUseCase.execute()
    }
  }
}
