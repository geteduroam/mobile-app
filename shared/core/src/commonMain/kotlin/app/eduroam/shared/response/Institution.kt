package app.eduroam.shared.response

import kotlinx.serialization.Serializable

@Serializable
data class Institution(
    val cat_idp: Int,
    val country: String,
    val id: String,
    val name: String,
)
