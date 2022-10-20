package app.eduroam.geteduroam.oauth

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.response.Profile
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun OAuthScreen(
    url: String,
    profile: Profile,
    viewModel: OAuthViewModel,
    goToConfigScreen: (WifiConfigData) -> Unit,
) {
    val state = rememberWebViewState(url = url)
    val configData by viewModel.configData.collectAsStateWithLifecycle(null)
    configData?.let { wifiConfigData ->
        LaunchedEffect(wifiConfigData) {
            viewModel.clearWifiConfigData()
            goToConfigScreen(wifiConfigData)
        }
    }

    Scaffold(topBar = {
        EduTopAppBar(stringResource(R.string.name))
    }) {
        WebView(state = state, onCreated = { webView ->
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
        }, client = object : AccompanistWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val code = request?.url?.getQueryParameter("code").orEmpty()
                val institutionId = request?.url?.getQueryParameter("state").orEmpty()
                val error = request?.url?.getQueryParameter("error").orEmpty()

                val hasReceivedConnectCode = code.isNotEmpty() && institutionId.isNotEmpty()
                if (hasReceivedConnectCode) {
                    viewModel.onNavigatedToRedirectUri(
                        code = code,
                        institutionId = institutionId,
                        error = error,
                        profile = profile
                    )
                    return true
                }
                return false
            }
        })
    }

}