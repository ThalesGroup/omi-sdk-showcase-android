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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.navigation.ScreenNavigation
import com.onewelcome.showcaseapp.ui.screens.sections.SdkInitializationScreen
import com.onewelcome.showcaseapp.ui.screens.sections.SectionItem

@Composable
fun HomeScreen() {
  val homeNavController = rememberNavController()
  NavHost(navController = homeNavController, startDestination = ScreenNavigation.Home.route) {
    composable(ScreenNavigation.Home.route) { HomeScreenContent(onNavigateToSection = { homeNavController.navigate(it) }) }
    composable(ScreenNavigation.SdkInitialization.route) { SdkInitializationScreen(homeNavController) }
  }
}

@Composable
fun HomeScreenContent(onNavigateToSection: (route: String) -> Unit) {
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
      Sections(onNavigateToSection)
    }
  }
}

@Composable
private fun Sections(onNavigateToSection: (route: String) -> Unit) {
  getSections().forEach { section ->
    Section(section, onNavigateToSection)
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_sdk_initialization), ScreenNavigation.SdkInitialization)
  )
}


@Composable
private fun Section(section: SectionItem, onNavigateToSection: (route: String) -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    onClick = {
      onNavigateToSection.invoke(section.navigation.route)
    }
  ) {
    Text(modifier = Modifier.padding(16.dp), text = section.title)
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  HomeScreenContent {}
}
