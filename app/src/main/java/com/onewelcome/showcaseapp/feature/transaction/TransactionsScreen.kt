package com.onewelcome.showcaseapp.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onewelcome.core.R
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.feature.transaction.TransactionsViewModel.State
import com.onewelcome.showcaseapp.feature.transaction.TransactionsViewModel.TransactionItem

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
  LaunchedEffect(Unit) {
    viewModel.init()
  }
  TransactionsScreenContent(
    uiState = viewModel.uiState,
    onRefresh = viewModel::refresh,
    onAccept = viewModel::acceptTransaction,
    onDeny = viewModel::denyTransaction,
    onClearSuccess = viewModel::clearSuccessMessage,
    onClearError = viewModel::clearErrorMessage
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsScreenContent(
  uiState: State,
  onRefresh: () -> Unit,
  onAccept: (TransactionItem) -> Unit,
  onDeny: (TransactionItem) -> Unit,
  onClearSuccess: () -> Unit,
  onClearError: () -> Unit
) {
  Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshBox(
      isRefreshing = uiState.isLoading,
      onRefresh = onRefresh,
      modifier = Modifier.fillMaxSize()
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(Dimensions.mPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
      ) {
        item {
          HeaderCard(onRefresh = onRefresh, isLoading = uiState.isLoading)
        }

        when {
          uiState.isLoading && uiState.transactions.isEmpty() -> {
            item {
              LoadingState()
            }
          }
          uiState.errorMessage != null && uiState.transactions.isEmpty() -> {
            item {
              ErrorState(errorMessage = uiState.errorMessage, onRetry = onRefresh)
            }
          }
          uiState.transactions.isEmpty() -> {
            item {
              EmptyState()
            }
          }
          else -> {
            items(
              items = uiState.transactions,
              key = { it.transactionId }
            ) { transaction ->
              TransactionCard(
                transaction = transaction,
                isProcessing = uiState.processingTransactionId == transaction.transactionId,
                isAnyActionInProgress = uiState.isProcessingAction,
                onAccept = { onAccept(transaction) },
                onDeny = { onDeny(transaction) }
              )
            }
          }
        }
      }
    }

    // Success Snackbar
    uiState.successMessage?.let { message ->
      Snackbar(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(Dimensions.mPadding),
        action = {
          TextButton(onClick = onClearSuccess) {
            Text(stringResource(R.string.dismiss))
          }
        }
      ) {
        Text(message)
      }
    }

    // Error Snackbar (only when there are transactions, otherwise show error state)
    if (uiState.transactions.isNotEmpty() && uiState.errorMessage != null) {
      Snackbar(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(Dimensions.mPadding),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        action = {
          TextButton(onClick = onClearError) {
            Text(stringResource(R.string.dismiss))
          }
        }
      ) {
        Text(uiState.errorMessage)
      }
    }
  }
}

@Composable
private fun HeaderCard(onRefresh: () -> Unit, isLoading: Boolean) {
  ShowcaseCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.pending_transactions_title),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = stringResource(R.string.pending_transactions_description),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      IconButton(onClick = onRefresh, enabled = !isLoading) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
          )
        } else {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.retry),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun LoadingState() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(200.dp),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
private fun EmptyState() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.Notifications,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Dimensions.mPadding))
    Text(
      text = stringResource(R.string.no_pending_transactions),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.no_pending_transactions_hint),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = stringResource(R.string.error_occurred),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = errorMessage,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(Dimensions.mPadding))
    Button(onClick = onRetry) {
      Text(stringResource(R.string.retry))
    }
  }
}

@Composable
private fun TransactionCard(
  transaction: TransactionItem,
  isProcessing: Boolean,
  isAnyActionInProgress: Boolean,
  onAccept: () -> Unit,
  onDeny: () -> Unit
) {
  // Disable buttons if this transaction is processing OR if any other action is in progress
  val buttonsEnabled = !isProcessing && !isAnyActionInProgress
  
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(Dimensions.mPadding)
    ) {
      // Header with User Profile
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.transaction_user_profile, transaction.userProfileId),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = transaction.timestamp,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Message
      Text(
        text = transaction.message,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Expiration info
      Text(
        text = stringResource(R.string.transaction_expires_at, transaction.expiresAt),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(Dimensions.mPadding))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onDeny,
          enabled = buttonsEnabled,
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
          )
        ) {
          if (isProcessing) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp
            )
          } else {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(stringResource(R.string.deny))
          }
        }

        Button(
          onClick = onAccept,
          enabled = buttonsEnabled,
          modifier = Modifier.weight(1f)
        ) {
          if (isProcessing) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onPrimary
            )
          } else {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(stringResource(R.string.accept))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TransactionsScreenPreview() {
  TransactionsScreenContent(
    uiState = State(
      transactions = listOf(
        TransactionItem(
          request = com.onegini.mobile.sdk.android.model.entity.OneginiMobileAuthWithPushRequest(
            "tx-123",
            "Please confirm login",
            "user-1"
          ),
          transactionId = "tx-123",
          message = "Please confirm login to your account",
          userProfileId = "user-1",
          timestamp = "Jan 16, 12:00:00",
          expiresAt = "12:05:00"
        )
      )
    ),
    onRefresh = {},
    onAccept = {},
    onDeny = {},
    onClearSuccess = {},
    onClearError = {}
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
  TransactionsScreenContent(
    uiState = State(),
    onRefresh = {},
    onAccept = {},
    onDeny = {},
    onClearSuccess = {},
    onClearError = {}
  )
}
