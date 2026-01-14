package com.onewelcome.showcaseapp.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCardButton
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.navigation.Screens

@Composable
fun HomeScreen(homeNavController: NavController) {
  HomeScreenContent(onNavigateToSection = { homeNavController.navigate(it) })
}

@Composable
fun HomeScreenContent(onNavigateToSection: (route: String) -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(start = Dimensions.mPadding, end = Dimensions.mPadding, bottom = Dimensions.mPadding)
  ) {
    Image(
      modifier = Modifier.fillMaxWidth(),
      painter = painterResource(id = R.drawable.thales_logo),
      contentDescription = stringResource(id = R.string.content_description_logo)
    )
    Sections(onNavigateToSection)
  }
}

@Composable
private fun Sections(onNavigateToSection: (route: String) -> Unit) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
  ) {
    getSections().forEach { section ->
      ShowcaseCardButton(section.title, section.navigation.route, onNavigateToSection)
    }
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_sdk_initialization), Screens.SdkInitialization),
    SectionItem(stringResource(R.string.section_title_user_registration), Screens.UserRegistration),
    SectionItem(stringResource(R.string.section_title_user_authentication), Screens.UserAuthentication),
    SectionItem(stringResource(R.string.section_title_mobile_authentication), Screens.MobileAuthentication),
    SectionItem(stringResource(R.string.section_title_user_deregistration), Screens.UserDeregistration),
    SectionItem(stringResource(R.string.section_title_change_pin), Screens.ChangePin),
    SectionItem(stringResource(R.string.section_title_single_sign_on), Screens.SingleSignOn),
    SectionItem(stringResource(R.string.section_title_tokens), Screens.Tokens),
    SectionItem(stringResource(R.string.section_title_sdk_reset), Screens.SdkReset),
    SectionItem(stringResource(R.string.section_title_logout), Screens.Logout),
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  HomeScreenContent {}
}
