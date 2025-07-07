package com.onewelcome.showcaseapp.feature.userauthentication

import androidx.compose.foundation.layout.Arrangement
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
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.home.SectionItem
import com.onewelcome.showcaseapp.navigation.Screens

@Composable
fun UserAuthenticationScreen(
  navController: NavController
) {
  UserAuthenticationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onNavigateDeeper = { navController.navigate(it) },
  )
}

@Composable
private fun UserAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  onNavigateDeeper: (String) -> Unit,
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.user_authentication)) { onNavigateBack.invoke() }
    }) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      ShowcaseFeatureDescription("Add some nice description", "")
      Sections(onNavigateDeeper)
    }
  }
}

@Composable
private fun Sections(onNavigateToSection: (String) -> Unit) {
  getSections().forEach { section ->
    ShowcaseCard(section.title, section.navigation.route, onNavigateToSection)
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_pin_authentication), Screens.PinAuthentication)
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  UserAuthenticationScreenContent(
    onNavigateBack = {},
    onNavigateDeeper = {},
  )
}
