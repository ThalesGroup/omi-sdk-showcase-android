package com.onewelcome.showcaseapp.feature.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest
import com.onewelcome.core.usecase.AuthenticateWithPushUseCase
import com.onewelcome.core.usecase.DenyPendingTransactionUseCase
import com.onewelcome.core.usecase.GetPendingTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
  private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
  private val authenticateWithPushUseCase: AuthenticateWithPushUseCase,
  private val denyPendingTransactionUseCase: DenyPendingTransactionUseCase
) : ViewModel() {

  var uiState by mutableStateOf(State())
    private set

  private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  private val dateTimeFormatter = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())

  fun init() {
    if (uiState.transactions.isEmpty() && !uiState.isLoading) {
      fetchPendingTransactions()
    }
  }

  fun refresh() {
    fetchPendingTransactions()
  }

  private fun fetchPendingTransactions() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      getPendingTransactionsUseCase.execute()
        .onSuccess { transactions ->
          val transactionItems = transactions.map { request ->
            TransactionItem(
              request = request,
              transactionId = request.transactionId,
              message = request.message ?: "No message",
              userProfileId = request.userProfileId,
              timestamp = formatTimestamp(request.timestamp),
              expiresAt = formatExpirationTime(request.timestamp, request.timeToLiveSeconds)
            )
          }
          uiState = uiState.copy(
            isLoading = false,
            transactions = transactionItems,
            errorMessage = null
          )
        }
        .onFailure { error ->
          uiState = uiState.copy(
            isLoading = false,
            errorMessage = error.message ?: "Failed to fetch pending transactions"
          )
        }
    }
  }

  fun acceptTransaction(item: TransactionItem) {
    uiState = uiState.copy(processingTransactionId = item.transactionId)
    authenticateWithPushUseCase.execute(item.request)
    // Note: The result is handled via NotificationEventDispatcher
    // Remove from list after initiating
    removeTransactionFromList(item.transactionId)
  }

  fun denyTransaction(item: TransactionItem) {
    viewModelScope.launch {
      uiState = uiState.copy(processingTransactionId = item.transactionId)
      denyPendingTransactionUseCase.execute(item.request)
        .onSuccess {
          removeTransactionFromList(item.transactionId)
          uiState = uiState.copy(
            processingTransactionId = null,
            successMessage = "Transaction denied successfully"
          )
        }
        .onFailure { error ->
          uiState = uiState.copy(
            processingTransactionId = null,
            errorMessage = error.message ?: "Failed to deny transaction"
          )
        }
    }
  }

  fun clearSuccessMessage() {
    uiState = uiState.copy(successMessage = null)
  }

  fun clearErrorMessage() {
    uiState = uiState.copy(errorMessage = null)
  }

  private fun removeTransactionFromList(transactionId: String) {
    val updatedList = uiState.transactions.filterNot { it.transactionId == transactionId }
    uiState = uiState.copy(
      transactions = updatedList,
      processingTransactionId = null
    )
  }

  private fun formatTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply {
      timeInMillis = timestamp
    }
    return dateTimeFormatter.format(calendar.time)
  }

  private fun formatExpirationTime(timestamp: Long, timeToLiveSeconds: Int): String {
    val calendar = Calendar.getInstance().apply {
      timeInMillis = timestamp
      add(Calendar.SECOND, timeToLiveSeconds)
    }
    return timeFormatter.format(calendar.time)
  }

  data class State(
    val isLoading: Boolean = false,
    val transactions: List<TransactionItem> = emptyList(),
    val processingTransactionId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
  )

  data class TransactionItem(
    val request: OneginiMobileAuthWithPushRequest,
    val transactionId: String,
    val message: String,
    val userProfileId: String,
    val timestamp: String,
    val expiresAt: String
  )
}
