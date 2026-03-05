package com.onewelcome.showcaseapp.feature.mobileauth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.onewelcome.core.components.ShowcaseCardButton
import com.onewelcome.core.components.ShowcaseFeatureDescription
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.home.SectionItem
import com.onewelcome.showcaseapp.navigation.Screens


@Composable
fun MobileAuthenticationScreen(navController: NavHostController) {
  MobileAuthenticationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onNavigateDeeper = { navController.navigate(it) })
}

@Composable
private fun MobileAuthenticationScreenContent(
  onNavigateBack: () -> Unit,
  onNavigateDeeper: (String) -> Unit,
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.mobile_authentication)) { onNavigateBack.invoke() }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
      ShowcaseFeatureDescription(stringResource(R.string.mobile_authentication_description), Constants.DOCUMENTATION_MOBILE_AUTHENTICATION)
      Sections(onNavigateDeeper)
    }
  }
}

@Composable
private fun Sections(onNavigateToSection: (String) -> Unit) {
  getSections().forEach { section ->
    ShowcaseCardButton(section.title, section.navigation.route, onNavigateToSection)
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_mobile_authentication_enrollment), Screens.MobileAuthenticationEnrollment),
    SectionItem(stringResource(R.string.section_title_mobile_authentication_push_enrollment), Screens.MobileAuthenticationPushEnrollment),
    SectionItem(stringResource(R.string.section_title_mobile_authentication_with_otp), Screens.MobileAuthenticationWithOtp),
  )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
MobileAuthenticationScreenContent({}, {})
}
