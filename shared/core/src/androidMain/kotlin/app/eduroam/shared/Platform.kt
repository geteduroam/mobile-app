package app.eduroam.shared

import com.github.scribejava.core.pkce.PKCE
import com.github.scribejava.core.pkce.PKCEService

actual class Platform actual constructor() {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

object OAuth2Android {
    private lateinit var pkce: PKCE
    fun getAuthorizationUrl(
        institutionId: String,
        authorizationEndpoint: String?,
        redirectUri: String,
        clientId: String
    ): String {
        // each auth flow has a codeChallenge/codeVerifier pair
        pkce = PKCEService.defaultInstance().generatePKCE()
        val builder = StringBuilder()

        builder.append(authorizationEndpoint)
        builder.append("?response_type=code")
        builder.append("&code_challenge_method=S256")
        builder.append("&scope=eap-metadata")
        builder.append("&code_challenge=${pkce.codeChallenge}")
        builder.append("&redirect_uri=${redirectUri}")
        builder.append("&client_id=${clientId}")
        builder.append("&state=$institutionId")

        return builder.toString()
    }

    fun getCodeVerifier(): String = pkce.codeVerifier
}

actual class OAuth2 {
    actual fun getAuthorizationUrl(
        institutionId: String,
        authorizationEndpoint: String?,
        redirectUri: String,
        clientId: String
    ): String =
        OAuth2Android.getAuthorizationUrl(
            institutionId = institutionId,
            authorizationEndpoint = authorizationEndpoint,
            redirectUri = redirectUri,
            clientId = clientId
        )

    actual fun getCodeVerifier(): String = OAuth2Android.getCodeVerifier()
}
