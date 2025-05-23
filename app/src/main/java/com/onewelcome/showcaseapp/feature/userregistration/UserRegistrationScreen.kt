package com.onewelcome.showcaseapp.feature.userregistration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCard
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.home.SectionItem
import com.onewelcome.showcaseapp.navigation.Screens

@Composable
fun UserRegistrationScreen(
  navController: NavController,
) {
  UserRegistrationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onNavigateDeeper = { navController.navigate(it) },
  )
}

@Composable
private fun UserRegistrationScreenContent(
  onNavigateBack: () -> Unit,
  onNavigateDeeper: (String) -> Unit,
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.user_registration)) { onNavigateBack.invoke() }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
    ) {
      ShowcaseFeatureDescription(stringResource(R.string.user_registration_description), Constants.DOCUMENTATION_USER_REGISTRATION)
      Sections(onNavigateDeeper)
    }
  }
}

@Composable
fun Sections(onNavigateToSection: (String) -> Unit) {
  getSections().forEach { section ->
    ShowcaseCard(section.title, section.navigation.route, onNavigateToSection)
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_browser_registration), Screens.BrowserRegistration)
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  UserRegistrationScreenContent(
    onNavigateBack = {},
    onNavigateDeeper = {},
  )
}
