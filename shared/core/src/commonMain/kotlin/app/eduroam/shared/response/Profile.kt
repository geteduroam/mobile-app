package app.eduroam.shared.response

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val eapconfig_endpoint: String? = null,
    val id: String,
    val name: String,
    val oauth: Boolean = false,
    val authorization_endpoint: String? = null,
    val token_endpoint: String? = null,
)