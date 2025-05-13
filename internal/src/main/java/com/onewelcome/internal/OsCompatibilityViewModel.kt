package com.onewelcome.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestCategory
import com.onewelcome.internal.entity.TestStatus
import com.onewelcome.internal.testcases.browserregistation.BrowserRegistrationTestCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OsCompatibilityViewModel @Inject constructor(
  private val browserRegistrationTestCases: BrowserRegistrationTestCases
) : ViewModel() {
  private val testCategories = listOf(browserRegistrationTestCases.tests)

  var uiState by mutableStateOf(State(testCategories = testCategories))
    private set

  fun onEvent(event: UiEvent) {
    when (event) {
      is UiEvent.RunTests -> runTests()
    }
  }

  private fun runTests() {
    uiState = uiState.copy(isLoading = true)
    markAllTestsAsRunning()
    viewModelScope.launch {
      runTestsSequentially()
      evaluateResult()
    }
  }

  private fun markAllTestsAsRunning() {
    uiState = uiState.copy(
      testCategories = uiState.testCategories.map { category ->
        category.copy(testCases = category.testCases.map { it.copy(status = TestStatus.Running) })
      })
  }

  private suspend fun runTestsSequentially() {
    for ((categoryIndex, category) in uiState.testCategories.withIndex()) {
      for ((caseIndex, testCase) in category.testCases.withIndex()) {
        val result = withContext(Dispatchers.Default) { runTest(testCase) }
        updateTestCase(categoryIndex, caseIndex, result)
      }
    }
  }

  private fun updateTestCase(featureIndex: Int, caseIndex: Int, resultStatus: TestStatus) {
    val currentCategory = uiState.testCategories[featureIndex]
    val updatedCases = currentCategory.testCases.toMutableList().apply {
      this[caseIndex] = this[caseIndex].copy(status = resultStatus)
    }
    val updatedCategories = uiState.testCategories.toMutableList().apply {
      this[featureIndex] = currentCategory.copy(testCases = updatedCases)
    }
    uiState = uiState.copy(testCategories = updatedCategories)
  }

  private fun evaluateResult() {
    val failedTestCases = uiState.testCategories
      .flatMap { it.testCases }
      .filter { it.status == TestStatus.Failed }
      .map { it.name }

    uiState = uiState.copy(
      testResult = evaluateTestResult(failedTestCases),
      isLoading = false
    )
  }

  private fun evaluateTestResult(failed: List<String>): Result<Unit, String> = if (failed.isEmpty()) {
    Ok(Unit)
  } else {
    Err(failed.addNewLineSeparator())
  }

  private suspend fun runTest(testCase: TestCase): TestStatus {
    return testCase.testFunction.invoke()
  }

  private fun List<String>.addNewLineSeparator(): String = joinToString(separator = "\n")

  data class State(
    val testCategories: List<TestCategory>,
    val testResult: Result<Unit, String>? = null,
    val isLoading: Boolean = false,
  )

  sealed interface UiEvent {
    data object RunTests : UiEvent
  }
}
