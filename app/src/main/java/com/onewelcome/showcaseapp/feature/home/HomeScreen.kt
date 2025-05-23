package com.onewelcome.showcaseapp.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseCard
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
      .padding(start = Dimensions.mPadding, end = Dimensions.mPadding)
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
  getSections().forEach { section ->
    ShowcaseCard(section.title, section.navigation.route, onNavigateToSection)
  }
}

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_sdk_initialization), Screens.SdkInitialization),
    SectionItem(stringResource(R.string.section_title_user_registration), Screens.UserRegistration)
  )
}


@Composable
private fun Section(section: SectionItem, onNavigateToSection: (route: String) -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth(),
    onClick = {
      onNavigateToSection.invoke(section.navigation.route)
    }
  ) {
    Row(
      modifier = Modifier.padding(Dimensions.mPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Dimensions.mPadding)
    ) {
      Text(
        modifier = Modifier
          .weight(1f),
        text = section.title,
        style = MaterialTheme.typography.titleMedium
      )
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = Icons.AutoMirrored.Filled.KeyboardArrowRight.name,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  HomeScreenContent {}
}
