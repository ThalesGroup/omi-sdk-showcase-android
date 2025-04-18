import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestFeature
import com.onewelcome.internal.entity.TestStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OsCompatibilityViewModel : ViewModel() {
  private val dummyTests = List(10) { featureIndex ->
    TestFeature(
      name = "Feature ${featureIndex + 1}",
      testCases = List(10) { testCaseIndex ->
        TestCase(
          name = "Feature ${featureIndex + 1} TestCase ${testCaseIndex + 1}"
        )
      }
    )
  }

  var testResult: Result<Unit, List<String>>? by mutableStateOf(null)
    private set

  var testFeatures: List<TestFeature> by mutableStateOf(dummyTests)
    private set

  fun runTests() {
    markAllTestsAsRunning()
    viewModelScope.launch {
      runTestsParallely().awaitAll()
      evaluateFinalResult()
    }
  }

  private fun markAllTestsAsRunning() {
    testFeatures = testFeatures.map { feature ->
      feature.copy(testCases = feature.testCases.map { it.copy(status = TestStatus.Running) })
    }
  }

  private fun CoroutineScope.runTestsParallely(): List<Deferred<Unit>> =
    testFeatures.flatMapIndexed { featureIndex, feature ->
      feature.testCases.mapIndexed { caseIndex, testCase ->
        async {
          val result = withContext(Dispatchers.Default) { runTest(testCase) }
          updateTestCase(featureIndex, caseIndex, result)
        }
      }
    }

  private fun updateTestCase(featureIndex: Int, caseIndex: Int, resultStatus: TestStatus) {
    val currentFeature = testFeatures[featureIndex]
    val updatedCases = currentFeature.testCases.toMutableList().apply {
      this[caseIndex] = this[caseIndex].copy(status = resultStatus)
    }
    val updatedFeatures = testFeatures.toMutableList().apply {
      this[featureIndex] = currentFeature.copy(testCases = updatedCases)
    }
    testFeatures = updatedFeatures
  }

  private fun evaluateFinalResult() {
    val allCases = testFeatures.flatMap { it.testCases }
    val failed = allCases
      .filter { it.status == TestStatus.Failed }
      .map { it.name }

    testResult = if (failed.isEmpty()) {
      Ok(Unit)
    } else {
      Err(failed)
    }
  }

  private suspend fun runTest(testCase: TestCase): TestStatus {
    return withContext(Dispatchers.Default) {
      Thread.sleep(100)
      if (Math.random() > 0.1) TestStatus.Passed else TestStatus.Failed
    }
  }
}
