import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OsCompatibilityViewModel : ViewModel() {
  private val _testItems = mutableStateListOf<TestItem>()
  val testItems: List<TestItem> get() = _testItems

  init {
    _testItems.addAll(
      List(10) { index ->
        TestItem(name = "Test ${index + 1}")
      }
    )
  }

  fun runTests() {
    _testItems.forEachIndexed { index, item ->
      _testItems[index] = item.copy(status = TestStatus.Running)
      viewModelScope.launch {
        val result = runTest(item)
        _testItems[index] = item.copy(status = result)
      }
    }
  }

  private suspend fun runTest(testItem: TestItem): TestStatus {
    return withContext(Dispatchers.Default) {
      Thread.sleep(1000)
      if (Math.random() > 0.5) TestStatus.Passed else TestStatus.Failed
    }
  }
}

data class TestItem(val name: String, var status: TestStatus = TestStatus.Pending)

enum class TestStatus {
  Pending,
  Running,
  Passed,
  Failed
}
