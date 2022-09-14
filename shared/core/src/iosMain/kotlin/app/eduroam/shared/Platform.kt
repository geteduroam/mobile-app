package app.eduroam.shared

import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual val platform: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}


actual class OAuth2 actual constructor() {
    actual fun getAuthorizationUrl(
        institutionId: String,
        authorizationEndpoint: String?,
        redirectUri: String,
        clientId: String
    ): String {
        return ""
    }

    actual fun getCodeVerifier(): String {
        TODO("Not yet implemented")
    }
}