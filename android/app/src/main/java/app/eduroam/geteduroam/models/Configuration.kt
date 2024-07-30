package app.eduroam.geteduroam.models

import android.net.Uri
import android.os.Parcelable
import app.eduroam.geteduroam.util.serializer.UriSerializer
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class Configuration(
    @SerialName("client_id") val clientId: String?,
    @SerialName("authorization_scope") val scope: String,
    @SerialName("redirect_uri") val redirect: String,
    @SerialName("end_session_redirect_uri") val endSessionRedirect: String? = null,
    @SerialName("discovery_uri") val discovery: String? = null,
    @SerialName("authorization_endpoint_uri") val authEndpoint: String,
    @SerialName("token_endpoint_uri") val tokenEndpoint: String,
    @SerialName("user_info_endpoint_uri") val endSessionEndpoint: String? = null,
    @SerialName("registration_endpoint_uri") val registrationEndpoint: String? = null,
    @SerialName("https_required") val isHttpsRequired: Boolean = true,
) : Parcelable {
    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val redirectUri: Uri = Uri.parse(redirect)

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val endSessionRedirectUri: Uri? = endSessionRedirect?.let { Uri.parse(it) }

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val discoveryUri: Uri? = discovery?.let { Uri.parse(it) }

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val authEndpointUri: Uri = Uri.parse(authEndpoint)

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val tokenEndpointUri: Uri = Uri.parse(tokenEndpoint)

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val endSessionEndpointUri: Uri? = endSessionEndpoint?.let { Uri.parse(it) }

    @IgnoredOnParcel
    @Serializable(with = UriSerializer::class)
    val registrationEndpointUri: Uri? = registrationEndpoint?.let { Uri.parse(it) }

    companion object {
        val EMPTY = Configuration(
            clientId = "",
            scope = "",
            redirect = "",
            endSessionRedirect = "",
            discovery = "",
            authEndpoint = "",
            tokenEndpoint = "",
            endSessionEndpoint = "",
            registrationEndpoint = "",
            isHttpsRequired = true

        )
    }
}