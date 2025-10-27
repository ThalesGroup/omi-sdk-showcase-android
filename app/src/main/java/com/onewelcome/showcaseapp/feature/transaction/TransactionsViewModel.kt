package com.onewelcome.showcaseapp.feature.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.onegini.mobile.sdk.android.handlers.error.OneginiPendingMobileAuthWithPushRequestError
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.usecase.GetPendingTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
  private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())

  fun init() {
    viewModelScope.launch {
      uiState = uiState.copy(getPendingTransactionsUseCase.execute())
    }
  }

  data class State(
    val result: Result<Set<OneginiMobileAuthWithPushRequest>, OneginiPendingMobileAuthWithPushRequestError>? = null
  )
}
