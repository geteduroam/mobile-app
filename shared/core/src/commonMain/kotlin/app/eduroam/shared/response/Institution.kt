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

    fun requiresAuth(profile: Profile? = null) = if (profiles.size == 1 || profile == null) {
        profiles[0].oauth
    } else if (profile != null) {
        profile.oauth
    } else {
        false
    }

}
