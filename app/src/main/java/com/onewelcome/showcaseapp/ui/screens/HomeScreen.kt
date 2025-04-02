package com.onewelcome.showcaseapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.navigation.Screens

@Composable
fun HomeScreen(navController: NavController) {
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column {
      Image(
        modifier = Modifier.fillMaxWidth(),
        painter = painterResource(id = R.drawable.thales_logo),
        contentDescription = stringResource(id = R.string.logo_content_description)
      )
      Sections(navController)
    }
  }
}

@Composable
private fun Sections(navController: NavController) {
  val sections = listOf("SDK initialization")
  sections.forEach { section ->
    Section(section, navController)
  }
}

@Preview(showBackground = true)
@Composable
private fun Section(@PreviewParameter(SectionDataProvider::class) section: String, navController: NavController) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    onClick = {
      navController.navigate(Screens.SdkInitialization.route)
    }
  ) {
    Text(modifier = Modifier.padding(16.dp), text = section)
  }
}

private class SectionDataProvider : PreviewParameterProvider<String> {
  override val values: Sequence<String>
    get() = sequenceOf(
      "Section 1",
      "Section 2",
      "Section 3"
    )
}
