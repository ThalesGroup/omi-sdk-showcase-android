package com.onewelcome.showcaseapp.viewmodel

import androidx.lifecycle.ViewModel
import com.onewelcome.showcaseapp.usecase.OmiSdkInitializationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SdkInitializationViewModel @Inject constructor(
  private val omiSdkInitializationUseCase: OmiSdkInitializationUseCase
) : ViewModel() {
  fun init() {
    omiSdkInitializationUseCase.initialize()
  }
}
