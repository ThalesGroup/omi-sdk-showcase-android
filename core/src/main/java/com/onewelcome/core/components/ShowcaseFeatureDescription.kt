package com.onewelcome.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.onewelcome.core.theme.Dimensions
import com.onewelcome.showcaseapp.R

@Composable
fun ShowcaseFeatureDescription(description: String, link: String) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing),
      modifier = Modifier.padding(Dimensions.mPadding)
    ) {
      Text(
        style = MaterialTheme.typography.bodyLarge,
        text = description
      )
      Text(
        style = MaterialTheme.typography.bodyLarge,
        text = buildAnnotatedString {
          append(stringResource(R.string.read_more) + " ")
          withLink(
            LinkAnnotation.Url(
              link,
              TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary))
            )
          ) {
            append(stringResource(R.string.here))
          }
        })
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  ShowcaseFeatureDescription("Description", "www.thalesgroup.com")
}