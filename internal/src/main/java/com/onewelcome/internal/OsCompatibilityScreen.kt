import android.os.Build
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.BuildConfig
import com.onewelcome.showcaseapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsCompatibilityScreen(viewModel: OsCompatibilityViewModel = hiltViewModel()) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.standardPadding),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    AndroidVersionInfoSection()
    LazyColumn(
      modifier = Modifier.weight(1f)
    ) {
      items(viewModel.testCases) { testCase -> TestItem(testCase) }
    }
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = Dimensions.smallPadding)
        .height(Dimensions.actionButtonHeight),
      onClick = { viewModel.runTests() }
    ) {
      Text(stringResource(R.string.run_tests))
    }
    Row(
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("OS compatibility program test result")
      return if (viewModel.testCases.any { it.status == TestStatus.Failed }) {
        TestStatusIcon(TestStatus.Failed)
      } else {
        TestStatusIcon(TestStatus.Pending)
      }
    }
    Button(
      modifier = Modifier
        .padding(top = Dimensions.smallPadding)
        .fillMaxWidth()
        .height(Dimensions.actionButtonHeight),
      onClick = { viewModel.runTests() }
    ) {
      Text(stringResource(R.string.save_result))
    }
  }
}

@Composable
private fun AndroidVersionInfoSection() {
  val sdkInt = Build.VERSION.SDK_INT
  val release = Build.VERSION.RELEASE ?: "Unknown"
  val codename = Build.VERSION.CODENAME ?: "Unknown"
  val previewSdk = Build.VERSION.PREVIEW_SDK_INT
  val isPreview = previewSdk > 0 || codename != "REL"

  Column {
    Text(
      modifier = Modifier.padding(bottom = Dimensions.smallPadding),
      text = "Android OS info",
      style = MaterialTheme.typography.titleMedium
    )
    Text("Release: $release")
    Text("API Level: $sdkInt")
    Text("Codename: $codename")
    Text("Preview SDK: $previewSdk")
    Text("Type: ${if (isPreview) "Beta/Preview version" else "Stable release"}")
  }
}

@Composable
private fun AppInfoSection() {
  val versionName = BuildConfig.VERSION_NAME
  val omiSdkVersion = BuildConfig.OMI_SDK_VERSION

  Column {
    Text(
      modifier = Modifier.padding(top = Dimensions.standardPadding, bottom = Dimensions.smallPadding),
      text = "Showcase app info",
      style = MaterialTheme.typography.titleMedium
    )
    Text("VersionName: $versionName")
    Text("OMI SDK version: $omiSdkVersion")
  }
}

@Composable
private fun TestItem(testCase: TestCase) {
  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.standardPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(testCase.name)
      TestStatusIcon(testCase.status)
    }
    HorizontalDivider()
  }
}

@Composable
private fun TestStatusIcon(status: TestStatus) {
  when (status) {
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
  OsCompatibilityScreen()
}
