package app.eduroam.geteduroam.models

import android.net.Uri
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class Configuration(
    @Json(name = "client_id") val clientId: String?,
    @Json(name = "authorization_scope") val scope: String,
    @Json(name = "redirect_uri") val redirect: String,
    @Json(name = "end_session_redirect_uri") val endSessionRedirect: String? = null,
    @Json(name = "discovery_uri") val discovery: String? = null,
    @Json(name = "authorization_endpoint_uri") val authEndpoint: String,
    @Json(name = "token_endpoint_uri") val tokenEndpoint: String,
    @Json(name = "user_info_endpoint_uri") val endSessionEndpoint: String? = null,
    @Json(name = "registration_endpoint_uri") val registrationEndpoint: String? = null,
    @Json(name = "https_required") val isHttpsRequired: Boolean = true,
) : Parcelable {
    @IgnoredOnParcel
    val redirectUri: Uri = Uri.parse(redirect)

    @IgnoredOnParcel
    val endSessionRedirectUri: Uri? = endSessionRedirect?.let { Uri.parse(it) }

    @IgnoredOnParcel
    val discoveryUri: Uri? = discovery?.let { Uri.parse(it) }

    @IgnoredOnParcel
    val authEndpointUri: Uri = Uri.parse(authEndpoint)

    @IgnoredOnParcel
    val tokenEndpointUri: Uri = Uri.parse(tokenEndpoint)

    @IgnoredOnParcel
    val endSessionEndpointUri: Uri? = endSessionEndpoint?.let { Uri.parse(it) }

    @IgnoredOnParcel
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