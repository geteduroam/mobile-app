package app.eduroam.geteduroam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.di.repository.NotificationRepository
import app.eduroam.geteduroam.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavController? = null

    private val job = Job()
    val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

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
        handleNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun handleNewIntent(intent: Intent?) {
        runOnUiThread {
            if (intent?.dataString?.startsWith("content://") == true) {
                CoroutineScope(coroutineContext).launch {
                    Route.ConfigureWifi.buildDeepLink(this@MainActivity, intent.data!!)?.let {
                        intent.data = Uri.parse(it)
                        withContext(Dispatchers.Main) {
                            navController?.handleDeepLink(intent)
                        }
                    }
                }
            } else {
                navController?.handleDeepLink(intent)
            }
        }
    }
}