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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OsCompatibilityViewModel : ViewModel() {
  private val dummyTests = List(10) { categoryIndex ->
    TestCategory(
      name = "Category ${categoryIndex + 1}",
      testCases = List(10) { testCaseIndex ->
        TestCase(
          name = "Category ${categoryIndex + 1} TestCase ${testCaseIndex + 1}"
        )
      }
    )
  }

  var uiState by mutableStateOf(State(testCategories = dummyTests))
    private set

  fun onEvent(event: UiEvent) {
    when (event) {
      UiEvent.runTests -> runTests()
      UiEvent.saveResults -> saveResults()
    }
  }

  private fun saveResults() {
    TODO("Not yet implemented")
  }

  private fun runTests() {
    uiState = uiState.copy(isLoading = true)
    markAllTestsAsRunning()
    viewModelScope.launch {
      runTestsParallely().awaitAll()
      evaluateResult()
    }
  }

  private fun markAllTestsAsRunning() {
    uiState = uiState.copy(
      testCategories = uiState.testCategories.map { feature ->
        feature.copy(testCases = feature.testCases.map { it.copy(status = TestStatus.Running) })
      })
  }

  private fun CoroutineScope.runTestsParallely(): List<Deferred<Unit>> =
    uiState.testCategories.flatMapIndexed { categoryIndex, feature ->
      feature.testCases.mapIndexed { caseIndex, testCase ->
        async {
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
    val allCases = uiState.testCategories.flatMap { it.testCases }
    val failed = allCases
      .filter { it.status == TestStatus.Failed }
      .map { it.name }

    uiState = uiState.copy(
      testResult = evaluateTestResult(failed),
      isLoading = false
    )
  }

  private fun evaluateTestResult(failed: List<String>): Result<Unit, String> = if (failed.isEmpty()) {
    Ok(Unit)
  } else {
    Err(failed.toEnrichedString())
  }

  private suspend fun runTest(testCase: TestCase): TestStatus {
    return withContext(Dispatchers.Default) {
      Thread.sleep(100)
      if (Math.random() > 0.1) TestStatus.Passed else TestStatus.Failed
    }
  }
}

private fun List<String>.toEnrichedString(separator: String = "\n"): String {
  return this.joinToString(separator = separator) { "❌ $it" }
}

data class State(
  val testCategories: List<TestCategory>,
  val testResult: Result<Unit, String>? = null,
  val isLoading: Boolean = false,
)

sealed interface UiEvent {
  data object runTests : UiEvent
  data object saveResults : UiEvent
}
