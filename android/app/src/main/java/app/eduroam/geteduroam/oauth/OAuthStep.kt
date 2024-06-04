package app.eduroam.geteduroam.oauth

import android.content.Intent
import android.net.Uri
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.ui.ErrorData
import net.openid.appauth.AuthorizationRequest

/**
 * Loading
 * Initialized with intent for launching OAuth
 * Launched && isAuthorizationLaunched == true, after intent was consumed and OAuth was started
 * ExchangingTokenRequest and isAuthorizationLaunched == false (intent response was consumed) and isFetchingToken == true
 * Authorized
 * */
sealed class OAuthStep {
    object Loading : OAuthStep()
    data class Initialized(val intent: Intent) : OAuthStep()
    data class WebViewFallback(val configuration: Configuration, val requestUri: Uri): OAuthStep()
    data class GetTokensFromRedirectUri(val redirectUri: Uri, val authRequest: AuthorizationRequest): OAuthStep()
    object Launched : OAuthStep()
    object ExchangingTokenRequest : OAuthStep()
    object Authorized : OAuthStep()
    object Error : OAuthStep()

    val isProcessing: Boolean
        get() = this is Loading || this is ExchangingTokenRequest

    override fun toString(): String = this.javaClass.simpleName
}

data class UiState(
    val oauthStep: OAuthStep,
    val error: ErrorData? = null,
)

