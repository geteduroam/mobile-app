package app.eduroam.geteduroam.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Institution(
    @Json(name = "cat_idp")
    val catIdp: Int,
    val country: String,
    val id: String,
    val name: String,
    val profiles: List<Profile>,
) : Parcelable {
    fun hasSingleProfile() = profiles.size == 1

    fun requiresAuth(): Boolean =
        if (hasSingleProfile()) {
            profiles[0].oauth
        } else false

}