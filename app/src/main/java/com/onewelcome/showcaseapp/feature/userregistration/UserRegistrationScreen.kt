package com.onewelcome.showcaseapp.feature.userregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.core.util.Constants
import com.onewelcome.showcaseapp.R
import com.onewelcome.showcaseapp.feature.home.SectionItem
import com.onewelcome.showcaseapp.feature.sdkinitialization.SdkInitializationViewModel
import com.onewelcome.showcaseapp.navigation.ScreenNavigation

@Composable
fun UserRegistrationScreen(
  navController: NavController,
) {
  Scaffold(
    topBar = {
      TopBar {
        navController.popBackStack()
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(start = Dimensions.mPadding, end = Dimensions.mPadding),
    ) {
      FeatureDescription()
      Sections({ navController.navigate(it) })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text("User registration") },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.content_description_navigate_back)
        )
      }
    })
}
