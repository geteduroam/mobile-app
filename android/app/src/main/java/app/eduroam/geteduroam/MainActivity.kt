package app.eduroam.geteduroam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import app.eduroam.geteduroam.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavController? = null

    private val job = Job()
    val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    val coroutineScope = CoroutineScope(coroutineContext)

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
                    },
                    openFileUri = {
                        coroutineScope.launch {
                            Timber.d("User has opened an .eap-config file using the built-in button")
                            if (!openFileUri(it)) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, R.string.err_msg_not_a_valid_eap_config_file, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
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
                coroutineScope.launch {
                    Timber.d("User has opened an .eap-config file with the app...")
                    if (!openFileUri(intent.data!!)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, R.string.err_msg_not_a_valid_eap_config_file, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                navController?.handleDeepLink(intent)
            }
        }
    }

    private suspend fun openFileUri(fileUri: Uri): Boolean {
        Route.ConfigureWifi.buildDeepLink(this@MainActivity, fileUri)?.let {
            intent.data = Uri.parse(it)
            return withContext(Dispatchers.Main) {
                return@withContext navController?.handleDeepLink(intent) ?: false
            }
        }
        return false
    }
}