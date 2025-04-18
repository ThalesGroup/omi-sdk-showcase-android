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
import com.onewelcome.core.components.ShowcaseExpandableCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestStatus
import com.onewelcome.showcaseapp.BuildConfig
import com.onewelcome.showcaseapp.R

private const val RELEASE_CODENAME = "REL"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsCompatibilityScreen(viewModel: OsCompatibilityViewModel = hiltViewModel()) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    AndroidVersionInfoSection()
    AppInfoSection()
    Button(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = Dimensions.mPadding)
        .height(Dimensions.actionButtonHeight),
      onClick = { viewModel.runTests() }
    ) {
      Text(stringResource(R.string.run_tests))
    }
    Text(
      modifier = Modifier.padding(top = Dimensions.mPadding),
      text = "Tests",
      style = MaterialTheme.typography.titleMedium
    )
    //TODO: Make visible only once test finished
//    Button(
//      modifier = Modifier
//        .padding(top = Dimensions.smallPadding)
//        .fillMaxWidth()
//        .height(Dimensions.actionButtonHeight),
//      onClick = { viewModel.runTests() }
//    ) {
//      Text(stringResource(R.string.save_result))
//    }
//    Row(
//      horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//      Text(stringResource(R.string.test_results))
//      return if (viewModel.testCases.any { it.status == TestStatus.Failed }) {
//        TestStatusIcon(TestStatus.Failed)
//      } else {
//        TestStatusIcon(TestStatus.Pending)
//      }
//    }
    LazyColumn(
      modifier = Modifier.weight(1f)
    ) {
      items(viewModel.testFeatures) { testFeature ->
        ShowcaseExpandableCard(
          modifier = Modifier.padding(top = Dimensions.sPadding),
          title = testFeature.name
        ) { TestFeatureSection(testFeature.testCases) }
      }
    }
  }
}

@Composable
private fun TestFeatureSection(testCases: List<TestCase>) {
  Column(
    modifier = Modifier.padding(top = Dimensions.sPadding)
  ) {
    testCases.forEach { TestItem(it) }
  }
}

@Composable
private fun AndroidVersionInfoSection() {
  val sdkVersion = Build.VERSION.RELEASE
  val apiLevel = Build.VERSION.SDK_INT
  val codename = if (Build.VERSION.CODENAME == RELEASE_CODENAME) stringResource(R.string.release) else Build.VERSION.CODENAME
  val previewSdkVersion = Build.VERSION.PREVIEW_SDK_INT
  val isPreview = previewSdkVersion > 0 || Build.VERSION.CODENAME != RELEASE_CODENAME
  val type = if (isPreview) stringResource(R.string.beta_preview_version, previewSdkVersion) else stringResource(R.string.stable_release)

  Column {
    Text(
      modifier = Modifier.padding(bottom = Dimensions.sPadding),
      text = stringResource(R.string.android_os),
      style = MaterialTheme.typography.titleMedium
    )
    Text(stringResource(R.string.sdk_version, sdkVersion))
    Text(stringResource(R.string.api_level, apiLevel))
    Text(stringResource(R.string.codename, codename))
    Text(stringResource(R.string.type, type))
  }
}

@Composable
private fun AppInfoSection() {
  val versionName = BuildConfig.VERSION_NAME
  val omiSdkVersion = BuildConfig.OMI_SDK_VERSION

  Column {
    Text(
      modifier = Modifier.padding(top = Dimensions.mPadding, bottom = Dimensions.sPadding),
      text = stringResource(R.string.app_name),
      style = MaterialTheme.typography.titleMedium
    )
    Text(stringResource(R.string.version, versionName))
    Text(stringResource(R.string.omi_sdk_version, omiSdkVersion))
  }
}

@Composable
private fun TestItem(testCase: TestCase) {
  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.mPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(testCase.name)
      TestStatusIcon(testCase.status)
    }
  }
}

@Composable
private fun TestStatusIcon(status: TestStatus) {
  when (status) {
    TestStatus.Pending -> Text("Pending")
    TestStatus.Running -> CircularProgressIndicator(
      modifier = Modifier.size(Dimensions.mPadding)
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
