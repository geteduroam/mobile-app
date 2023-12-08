package app.eduroam.geteduroam

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import app.eduroam.geteduroam.di.repository.NotificationRepository
import app.eduroam.geteduroam.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContent {
            AppTheme {
                navController = MainGraph(
                    closeApp = {
                        this@MainActivity.finish()
                    })
            }
        }
        runOnUiThread {
            navController?.handleDeepLink(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        runOnUiThread {
            navController?.handleDeepLink(intent)
        }
    }
}