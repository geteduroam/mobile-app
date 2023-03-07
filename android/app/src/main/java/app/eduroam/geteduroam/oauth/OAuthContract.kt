package app.eduroam.geteduroam.oauth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import app.eduroam.geteduroam.Screens
import app.eduroam.shared.response.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.util.*

class OAuthContract(
    authEndpoint: String,
    tokenEndpoint: String,
    private val handleResponse: (
        intentData: Intent,
    ) -> Unit
) : ActivityResultContract<String, String>() {

    private val authorizationEndpoint: Uri = Uri.parse(authEndpoint)
    private val tokenEndpoint: Uri = Uri.parse(tokenEndpoint)
    var service: AuthorizationService? = null

    override fun createIntent(context: Context, institutionId: String): Intent {
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(AnyBrowserMatcher.INSTANCE)
        builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
        val authRequest = AuthorizationRequest.Builder(
            /* configuration = */AuthorizationServiceConfiguration(
                authorizationEndpoint, tokenEndpoint
            ),
            /* clientId = */ Screens.OAuth.APP_ID,
            /* responseType = */ ResponseTypeValues.CODE,
            /* redirectUri = */ Uri.parse("${Screens.OAuth.APP_ID}:/")
        ).setState(institutionId).setNonce(UUID.randomUUID().toString())
            .setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier()).setScope("eap-metadata")
            .build()

        service = AuthorizationService(context, builder.build())

        return service?.getAuthorizationRequestIntent(
            /*request = */authRequest
        ) ?: Intent()
    }


    override fun parseResult(resultCode: Int, intent: Intent?): String {
        Log.d("contract", "parseResult() called with: resultCode = $resultCode, intent = $intent")
        val intentData = if (intent != null) {
            intent
        } else {
            Log.e("OAuthContract", "Did not receive valid OAuth intent back")
            return ""
        }
//        handleResponse(service, intentData)
        return ""
    }
}