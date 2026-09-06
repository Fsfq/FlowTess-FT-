package com.example

import android.content.pm.ActivityInfo
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
  companion object {
    init {
      try {
        System.loadLibrary("EOSSDK")
        System.loadLibrary("eos_bridge")
        android.util.Log.i("MainActivity", "EOS native libraries loaded in static block")
      } catch (t: Throwable) {
        android.util.Log.e("MainActivity", "Failed to load EOS native libraries in static block: ${t.message}", t)
      }
    }
  }

  private lateinit var mainViewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Initialize Epic Online Services SDK early
    com.example.eos.EosManager.init(this)

    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    enableEdgeToEdge()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      window.isNavigationBarContrastEnforced = false
    }
    val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      val themeName by mainViewModel.themeColor.collectAsStateWithLifecycle()
      val fontKey by mainViewModel.customFontKey.collectAsStateWithLifecycle()
      MyApplicationTheme(themeName = themeName, fontKey = fontKey) {
        TetrisApp(mainViewModel)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    if (::mainViewModel.isInitialized) {
      mainViewModel.onAppResume()
    }
  }

  override fun onStop() {
    super.onStop()
    if (::mainViewModel.isInitialized) {
      mainViewModel.onAppPause()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    com.example.eos.EosManager.onDestroy()
  }
}
