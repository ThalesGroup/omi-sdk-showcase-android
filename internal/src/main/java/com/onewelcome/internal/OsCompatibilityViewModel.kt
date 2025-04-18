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
          name = "TestCase ${testCaseIndex + 1}"
        )
      }
    )
  }

  var testResult: Result<List<TestCase>, List<TestCase>>? by mutableStateOf(null)
    private set

  var testFeatures: List<TestFeature> by mutableStateOf(dummyTests)
    private set

  fun runTests() {
    // Step 1: Mark all test cases as Running
    testFeatures = testFeatures.map { feature ->
      feature.copy(
        testCases = feature.testCases.map { it.copy(status = TestStatus.Running) }
      )
    }

    viewModelScope.launch {
      val jobs = mutableListOf<Deferred<Unit>>()

      testFeatures.forEachIndexed { featureIndex, feature ->
        feature.testCases.forEachIndexed { caseIndex, testCase ->
          val job = async {
            val result = withContext(Dispatchers.Default) {
              runTest(testCase)
            }

            // Step 2: Apply update immediately for this test case
            val updatedFeature = testFeatures[featureIndex]
            val updatedCases = updatedFeature.testCases.toMutableList().apply {
              this[caseIndex] = testCase.copy(status = result)
            }

            val updatedList = testFeatures.toMutableList().apply {
              this[featureIndex] = updatedFeature.copy(testCases = updatedCases)
            }

            testFeatures = updatedList // triggers recomposition immediately
          }

          jobs += job
        }
      }

      // Step 3: Wait for all tests to complete
      jobs.awaitAll()

      // Step 4: Emit final test result
      val allTestCases = testFeatures.flatMap { it.testCases }
      val failedTests = allTestCases.filter { it.status == TestStatus.Failed }

      testResult = if (failedTests.isEmpty()) {
        Ok(allTestCases)
      } else {
        Err(failedTests)
      }
    }
  }


  private suspend fun runTest(testCase: TestCase): TestStatus {
    return withContext(Dispatchers.Default) {
      Thread.sleep(500)
      if (Math.random() > 0.5) TestStatus.Passed else TestStatus.Failed
    }
  }
}
