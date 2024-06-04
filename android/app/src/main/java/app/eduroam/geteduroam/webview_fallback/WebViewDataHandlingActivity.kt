package app.eduroam.geteduroam.webview_fallback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import app.eduroam.geteduroam.MainActivity
import net.openid.appauth.AuthorizationManagementActivity
import net.openid.appauth.AuthorizationRequest

/**
 * With this activity we trick the AppAuth library into believing it is being redirected from its own flow while we did the flow ourselves in a WebView
 */
class WebViewDataHandlingActivity: AuthorizationManagementActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.putAll(intent.extras)
        super.onCreate(savedInstanceState)
    }

    companion object {

        fun createIntent(context: Context, authorizationRequest: AuthorizationRequest, redirectUri: Uri): Intent {
            val resultIntent = createStartIntent(
                context,
                authorizationRequest,
                Intent(),
                null,
                null
            )
            resultIntent.setClass(context, WebViewDataHandlingActivity::class.java)
            resultIntent.putExtra("authStarted", true)
            resultIntent.data = redirectUri
            return resultIntent
        }
    }
}