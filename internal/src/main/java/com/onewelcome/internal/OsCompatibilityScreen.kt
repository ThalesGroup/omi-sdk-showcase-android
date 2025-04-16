import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onewelcome.core.theme.Dimensions

@Composable
fun OsCompatibilityScreen(viewModel: OsCompatibilityViewModel = viewModel()) {
  val tests = viewModel.testItems
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.standardPadding),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    LazyColumn(
      modifier = Modifier.weight(1f)
    ) {
      items(tests) { testItem ->
        TestRow(testItem)
      }
    }

    Button(
      modifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = { viewModel.runTests() }
    ) {
      Text("Run Tests")
    }
  }
}

@Composable
private fun TestRow(testItem: TestItem) {
  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.standardPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(testItem.name)

      when (testItem.status) {
        TestStatus.Pending -> Text("Pending")
        TestStatus.Running -> CircularProgressIndicator(
          modifier = Modifier.size(Dimensions.standardPadding)
        )

        TestStatus.Passed -> Icon(
          Icons.Default.Check,
          contentDescription = "Passed",
          tint = Color.Green
        )

        TestStatus.Failed -> Icon(
          Icons.Default.Close,
          contentDescription = "Failed",
          tint = Color.Red
        )
      }
    }
    HorizontalDivider()
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  OsCompatibilityScreen()
}