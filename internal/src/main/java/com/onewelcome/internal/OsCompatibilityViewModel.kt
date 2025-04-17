import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OsCompatibilityViewModel : ViewModel() {
  val testCases: List<TestCase> get() = _testCases
  private val _testCases = mutableStateListOf<TestCase>()

  init {
    _testCases.addAll(
      List(15) { index ->
        TestCase(name = "Test case ${index + 1}")
      }
    )
  }

  fun runTests() {
    _testCases.forEachIndexed { index, item ->
      _testCases[index] = item.copy(status = TestStatus.Running)
      viewModelScope.launch {
        val result = runTest(item)
        _testCases[index] = item.copy(status = result)
      }
    }
  }

  private suspend fun runTest(testCase: TestCase): TestStatus {
    return withContext(Dispatchers.Default) {
      Thread.sleep(1000)
      if (Math.random() > 0.5) TestStatus.Passed else TestStatus.Failed
    }
  }
}

data class TestCase(val name: String, var status: TestStatus = TestStatus.Pending)

enum class TestStatus {
  Pending,
  Running,
  Passed,
  Failed
}
