package app.eduroam.shared

expect class Platform() {
    val platform: String
}

//Follow instructions from here to implement the native side
//https://github.com/geteduroam/lets-wifi/blob/master/API.md#authorization-endpoint
expect class OAuth2() {
    fun getAuthorizationUrl(
        institutionId: String,
        authorizationEndpoint: String?,
        redirectUri: String,
        clientId: String
    ): String

    fun getCodeVerifier(): String
}