package com.onewelcome.core.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onewelcome.showcaseapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseTopBar(title: String, onNavigateBack: () -> Unit) {
  TopAppBar(
    windowInsets = WindowInsets(0.dp),
    title = { Text(title) },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.content_description_navigate_back)
        )
      }
    })
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  ShowcaseTopBar("title") { }
}