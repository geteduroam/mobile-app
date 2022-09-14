package app.eduroam.shared.response

import kotlinx.serialization.Serializable

@Serializable
data class Institution(
    val cat_idp: Int,
    val country: String,
    val id: String,
    val name: String,
    val profiles: List<Profile>,
) {
    fun hasSingleProfile() = profiles.size == 1

    fun requiresAuth(): Boolean =
        if (hasSingleProfile()) {
            profiles[0].oauth
        } else false

}