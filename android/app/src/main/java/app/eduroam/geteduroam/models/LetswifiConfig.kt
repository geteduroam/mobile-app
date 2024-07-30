package app.eduroam.geteduroam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LetswifiConfig(
    @SerialName("eapconfig_endpoint")
    val eapConfigEndpoint: String?,
    @SerialName("mobileconfig_endpoint")
    val mobileConfigEndpoint: String?,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String?,
    @SerialName("token_endpoint")
    val tokenEndpoint: String?
) : Parcelable
