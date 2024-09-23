package app.eduroam.geteduroam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LetswifiConfig(
    @SerialName("eapconfig_endpoint")
    val eapConfigEndpoint: String? = null,
    @SerialName("mobileconfig_endpoint")
    val mobileConfigEndpoint: String? = null,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String? = null,
    @SerialName("token_endpoint")
    val tokenEndpoint: String? = null
) : Parcelable
