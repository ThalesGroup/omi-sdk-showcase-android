package com.onewelcome.showcaseapp.feature.userregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.onewelcome.core.components.ShowcaseTopBar
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.home.SectionItem
import com.onewelcome.showcaseapp.navigation.ScreenNavigation

@Composable
fun UserRegistrationScreen(
  navController: NavController,
) {
  UserRegistrationScreenContent(
    onNavigateBack = { navController.popBackStack() },
    onNavigateDeeper = { navController.navigate(it) }
  )
}

@Composable
private fun UserRegistrationScreenContent(
  onNavigateBack: () -> Unit,
  onNavigateDeeper: (String) -> Unit
) {
  Scaffold(
    topBar = {
      ShowcaseTopBar(stringResource(R.string.user_registration)) {
        onNavigateBack.invoke()
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
    ) {
      FeatureDescription()
      Sections { onNavigateDeeper }
    }
  }
}

@Composable
private fun FeatureDescription() {
  Column(verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)) {
    Text(
      style = MaterialTheme.typography.bodyLarge,
      text = stringResource(R.string.user_registration_description)
    )
    Text(
      style = MaterialTheme.typography.bodyLarge,
      text = buildAnnotatedString {
        append(stringResource(R.string.read_more) + " ")
        withLink(
          LinkAnnotation.Url(
            Constants.DOCUMENTATION_USER_REGISTRATION,
            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary))
          )
        ) {
          append(stringResource(R.string.here))
        }
      })
  }
}

@Composable
fun Sections(onNavigateToSection: (route: String) -> Unit) {
  getSections().forEach { section ->
    Section(section, onNavigateToSection)
  }
}

@Composable
private fun Section(section: SectionItem, onNavigateToSection: (route: String) -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = Dimensions.mPadding),
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

@Composable
@ReadOnlyComposable
private fun getSections(): List<SectionItem> {
  return listOf(
    SectionItem(stringResource(R.string.section_title_browser_registration), ScreenNavigation.BrowserRegistration)
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
