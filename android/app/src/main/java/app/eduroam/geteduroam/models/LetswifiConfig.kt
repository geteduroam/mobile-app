package app.eduroam.geteduroam.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class LetswifiConfig(
    @Json(name = "eapconfig_endpoint")
    val eapConfigEndpoint: String?,
    @Json(name = "mobileconfig_endpoint")
    val mobileConfigEndpoint: String?,
    @Json(name = "authorization_endpoint")
    val authorizationEndpoint: String?,
    @Json(name = "token_endpoint")
    val tokenEndpoint: String?
) : Parcelable
