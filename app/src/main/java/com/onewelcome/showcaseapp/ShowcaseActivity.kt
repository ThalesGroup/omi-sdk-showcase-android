package com.onewelcome.showcaseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.onewelcome.showcaseapp.navigation.BottomNavigationBar
import com.onewelcome.showcaseapp.ui.theme.ShowcaseAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowcaseActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ShowcaseAppTheme {
        BottomNavigationBar()
      }
    }
  }
}
