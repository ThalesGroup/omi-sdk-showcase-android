package com.onewelcome.internal

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onewelcome.core.components.ShowcaseExpandableCard
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.internal.OsCompatibilityViewModel.UiEvent
import com.onewelcome.internal.entity.TestCase
import com.onewelcome.internal.entity.TestStatus
import com.onewelcome.internal.util.TestResultFileCreator
import com.onewelcome.internal.util.appVersionInfo
import com.onewelcome.internal.util.osVersionInfo
import com.onewelcome.showcaseapp.R
import java.io.File

@Composable
fun OsCompatibilityScreen(viewModel: OsCompatibilityViewModel = hiltViewModel()) {
  val expandedCategories = remember { mutableStateMapOf<Int, Boolean>() }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(Dimensions.mPadding),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    item {
      AndroidVersionInfoSection()
      AppInfoSection()
      RunTestsButton(viewModel)
      TestResults(viewModel.uiState.testResult, viewModel)
      Text(
        modifier = Modifier.padding(top = Dimensions.mPadding),
        text = stringResource(R.string.tests),
        style = MaterialTheme.typography.titleMedium
      )
    }
    itemsIndexed(viewModel.uiState.testCategories) { index, testCategory ->
      val isExpanded = expandedCategories[index] == true
      ShowcaseExpandableCard(
        modifier = Modifier.padding(top = Dimensions.sPadding),
        title = testCategory.name,
        isExpanded = isExpanded,
        onExpandToggle = { expandedCategories[index] = !isExpanded }
      ) {
        TestCasesSection(testCategory.testCases)
      }
    }
  }
}

@Composable
private fun RunTestsButton(viewModel: OsCompatibilityViewModel) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = Dimensions.mPadding)
      .height(Dimensions.actionButtonHeight),
    onClick = { if (viewModel.uiState.isLoading.not()) viewModel.onEvent(UiEvent.RunTests) },
  ) {
    if (viewModel.uiState.isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Text(stringResource(R.string.run_tests))
    }
  }
}

@Composable
private fun TestCasesSection(testCases: List<TestCase>) {
  Column(
    modifier = Modifier.padding(top = Dimensions.sPadding)
  ) {
    testCases.forEach { TestItem(it) }
  }
}

@Composable
private fun AndroidVersionInfoSection() {
  Column {
    Text(
      modifier = Modifier.padding(bottom = Dimensions.sPadding),
      text = stringResource(R.string.android_os),
      style = MaterialTheme.typography.titleMedium
    )
    osVersionInfo().apply {
      Text(sdkVersion)
      Text(apiLevel)
      Text(codename)
      Text(type)
    }
  }
}

@Composable
private fun AppInfoSection() {
  Column {
    Text(
      modifier = Modifier.padding(top = Dimensions.mPadding, bottom = Dimensions.sPadding),
      text = stringResource(R.string.app_name),
      style = MaterialTheme.typography.titleMedium
    )
    appVersionInfo().apply {
      Text(version)
      Text(omiSdkVersion)
    }
  }
}

@Composable
private fun TestResults(testResult: Result<Unit, String>?, viewModel: OsCompatibilityViewModel) {
  Column {
    testResult
      ?.onSuccess {
        TestResultHeader()
        Text(stringResource(R.string.all_test_passed))
        SaveResultsButton(viewModel.uiState.testResult)
      }
      ?.onFailure {
        TestResultHeader()
        Text(stringResource(R.string.tests_failed_emoji))
        Text(it, modifier = Modifier.padding(top = Dimensions.sPadding))
        SaveResultsButton(viewModel.uiState.testResult)
      }
  }
}

@Composable
private fun SaveResultsButton(testResult: Result<Unit, String>?) {
  val context = LocalContext.current
  val androidVersionInfo = osVersionInfo()
  val appVersionInfo = appVersionInfo()
  val testResultSavedText = stringResource(R.string.test_result_saved)
  val testResultValue = getResultValue(testResult)
  val testResultFileContent = TestResultFileCreator.getFileContent(appVersionInfo, androidVersionInfo, testResultValue)
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = Dimensions.mPadding)
      .height(Dimensions.actionButtonHeight),
    onClick = {
      File(
        (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)),
        Constants.OS_COMPATIBILITY_TEST_RESULT_FILE_NAME
      )
        .writeText(testResultFileContent)
      Toast.makeText(context, testResultSavedText, Toast.LENGTH_LONG).show()
    }
  ) {
    Text(stringResource(R.string.save_result))
  }
}

@Composable
private fun getResultValue(testResult: Result<Unit, String>?): String? {
  return testResult?.let {
    if (it.isOk) {
      stringResource(R.string.test_succeed)
    } else {
      stringResource(R.string.tests_failed, "\n", testResult.error)
    }
  }
}

@Composable
private fun TestResultHeader() {
  Text(
    modifier = Modifier.padding(top = Dimensions.mPadding, bottom = Dimensions.mPadding),
    text = stringResource(R.string.test_results),
    style = MaterialTheme.typography.titleMedium
  )
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
    TestStatus.Pending -> Text(stringResource(R.string.pending))
    TestStatus.Running -> CircularProgressIndicator(
      modifier = Modifier.size(Dimensions.mPadding)
    )

    TestStatus.Passed -> Icon(
      Icons.Default.Check,
      contentDescription = stringResource(R.string.passed),
      tint = Color.Green
    )

    TestStatus.Failed -> Icon(
      Icons.Default.Close,
      contentDescription = stringResource(R.string.failed),
      tint = Color.Red
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  OsCompatibilityScreen()
}
