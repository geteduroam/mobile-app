package app.eduroam.geteduroam

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.eduroam.geteduroam.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContent {
            viewModel = hiltViewModel()
            AppTheme {
                MainGraph(
                    mainViewModel = viewModel,
                    closeApp = {
                        this@MainActivity.finish()
                    })
            }
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        runOnUiThread {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent?) {
        viewModel.openIntent = intent
    }
}