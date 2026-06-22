package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TetrisApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
      val mainViewModel: MainViewModel = viewModel(factory = factory)
      val themeName by mainViewModel.themeColor.collectAsStateWithLifecycle()
      val fontKey by mainViewModel.customFontKey.collectAsStateWithLifecycle()
      MyApplicationTheme(themeName = themeName, fontKey = fontKey) {
        TetrisApp(mainViewModel)
      }
    }
  }
}
