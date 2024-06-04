package app.eduroam.geteduroam.ui

import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import app.eduroam.geteduroam.BuildConfig
import timber.log.Timber

@Composable
fun OAuthWebView(startUrl: Uri, onRedirectUriFound: (Uri) -> Unit) {
    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(factory = {
        WebView(it).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            this.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.webViewClient = RedirectUriInterceptingWebViewClient(onRedirectUriFound)
        }
    }, update = {
        it.loadUrl(startUrl.toString())
    })
}

class RedirectUriInterceptingWebViewClient(private val onRedirectUriFound: (Uri) -> Unit) : WebViewClient() {

    private val redirectScheme = Uri.parse(BuildConfig.OAUTH_REDIRECT_URI).scheme

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        try {
            val uri = request?.url
            if (uri != null && uri.scheme == redirectScheme) {
                onRedirectUriFound(uri)
                return true
            }
        } catch (ex: Exception) {
            Timber.i("Could not parse navigation URI!", ex)
        }
        return false
    }
}