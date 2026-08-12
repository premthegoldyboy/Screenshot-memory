package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.screenshotmemory.ui.navigation.AppNavGraph
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isDarkTheme by rememberSaveable { mutableStateOf(false) }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        AppNavGraph(
          darkTheme = isDarkTheme,
          onThemeChange = { isDarkTheme = it }
        )
      }
    }
  }
}

